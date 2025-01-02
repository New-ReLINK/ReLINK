package com.my.relink.chat.service;

import com.my.relink.chat.aop.metric.TimeMetric;
import com.my.relink.chat.controller.dto.request.ChatMessageReqDto;
import com.my.relink.chat.controller.dto.response.ChatMessageRespDto;
import com.my.relink.common.notification.NotificationPublisherService;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.repository.MessageRepository;
import com.my.relink.domain.notification.chat.ChatStatus;
import com.my.relink.domain.trade.repository.dto.TradeWithOwnerItemNameDto;
import com.my.relink.domain.user.User;
import com.my.relink.service.TradeService;
import com.my.relink.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Profile("test")
public class ChatCacheService {

    private final MessageRepository messageRepository;
    private final TradeService tradeService;
    private final UserService userService;
    private final NotificationPublisherService notificationPublisherService;
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final RedisTemplate<String, String> redisLockTemplate;
    private static final long CACHE_EXPIRATION_MINUTES = 40;
    private static final String CHAT_TRADE_PREFIX = "chat:trade:*";
    private static final String CHAT_MESSAGE_CACHE_KEY = "chat:trade:";
    private static final long MAX_QUEUE_SIZE = 500; // 채팅방당 최대 메시지 수 500

    private static final String LOCKED = "LOCKED";
    private final Clock clock;



    @Async
    public CompletableFuture<Void> sendNotificationAsync(
            Long senderId,
            String content,
            String senderNickname,
            String itemName,
            ChatStatus status) {
        return CompletableFuture.runAsync(() ->
                notificationPublisherService.crateChatNotification(
                        senderId, content, senderNickname, itemName, status
                )
        );
    }

    @Transactional
    @TimeMetric
    public ChatMessageRespDto saveMessage(Long tradeId, ChatMessageReqDto dto, Long senderId) {
        LocalDateTime messageTime = LocalDateTime.now(clock.withZone(ZoneId.of("Asia/Seoul")));
        User sender = userService.findByIdOrFail(senderId);
        TradeWithOwnerItemNameDto tradeInfo = tradeService.findTradeWithOwnerItemName(tradeId);

        //Redis에 실시간 메시지 저장
        String cacheKey = CHAT_MESSAGE_CACHE_KEY + tradeId;
        Message message = dto.toEntity(tradeInfo.getTrade(), sender);
        ChatMessage chatMessage = new ChatMessage(message, messageTime);

        redisTemplate.opsForZSet().add(cacheKey, chatMessage, messageTime.toEpochSecond(ZoneOffset.UTC));
        redisTemplate.expire(cacheKey, CACHE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        sendNotificationAsync(senderId, chatMessage.getContent(), sender.getNickname(), tradeInfo.getItemName(), ChatStatus.NEW_CHAT);

        return new ChatMessageRespDto(chatMessage);
    }


    //@Scheduled(fixedRate = 1200000) // 20분
    @TimeMetric
    @Transactional
    public void persistCachedMessages() {
        String lockKey = "batch:persist:lock";
        Boolean acquired = redisLockTemplate.opsForValue().setIfAbsent(lockKey, LOCKED, 10, TimeUnit.MINUTES);

        if (acquired != null && !acquired) {
            log.debug("이미 배치 작업이 실행 중입니다");
            return;
        }

        try {
            List<ChatMessage> expiringSoonMessages = getExpiringSoonMessages();
            if (!expiringSoonMessages.isEmpty()) {
                List<Message> savedMessages = messageRepository.saveAll(expiringSoonMessages.stream()
                        .map(ChatMessage::toEntity)
                        .toList()
                );
                log.info("DB에 저장된 메시지 수: {}", savedMessages.size());
                removePersistedMessagesFromCache(expiringSoonMessages);
            }
        } finally {
            redisLockTemplate.delete(lockKey);
        }
    }

    private List<ChatMessage> getExpiringSoonMessages(){
        List<ChatMessage> expiringSoonMessages = new ArrayList<>();

        try{
            Set<String> chatKeys = redisTemplate.keys(CHAT_TRADE_PREFIX);
            log.info("검색된 채팅방 수: {}", chatKeys != null ? chatKeys.size() : 0);

            if(chatKeys == null || chatKeys.isEmpty()){
                return expiringSoonMessages;
            }

            //각 채팅방 별로 만료 시간 확인 및 메시지 수집
            for (String key : chatKeys) {
                boolean shouldPersist = false;
                Long ttl = redisTemplate.getExpire(key, TimeUnit.HOURS);
                Long queueSize = redisTemplate.opsForZSet().size(key);

                log.info("채팅방 상태 체크: key={}, ttl={}, queueSize={}, MAX_SIZE={}",
                        key, ttl, queueSize, MAX_QUEUE_SIZE);

                // TTL이 남은 시간 5분 이하 or 큐 크기가 초과된 경우
                if ((ttl != null && ttl < 5) || (queueSize != null && queueSize >= MAX_QUEUE_SIZE)) {
                    shouldPersist = true;
                }

                if(shouldPersist){
                    Set<ChatMessage> messages = redisTemplate.opsForZSet().range(key, 0, -1);
                    log.info("저장 대상 메시지 수: key={}, count={}", key,
                            messages != null ? messages.size() : 0);
                    if(!messages.isEmpty() && messages != null){
                        expiringSoonMessages.addAll(messages);
                    }
                }
            }
            log.info("총 저장 예정 메시지 수: {}", expiringSoonMessages.size());

        }catch (Exception e){
            log.error("메시지 수집 중 오류 발생: cause = {}", e.getMessage(), e);
            throw e;
        }
        return expiringSoonMessages;
    }

    private void removePersistedMessagesFromCache(List<ChatMessage> persistedMessages){
        try{
            Map<Long, List<ChatMessage>> messagesByTradeId = persistedMessages.stream()
                    .collect(Collectors.groupingBy(ChatMessage::getTradeId));

            for (Map.Entry<Long, List<ChatMessage>> entry : messagesByTradeId.entrySet()) {
                String key = CHAT_MESSAGE_CACHE_KEY + entry.getKey();
                redisTemplate.delete(key);
                log.debug("채팅 데이터 캐시 삭제 완료: key = {}", key);
            }
        }catch (Exception e){
            log.error("db에 저장된 메시지 캐시에서 삭제 중 오류 발생: cause = {}", e.getMessage(), e);
            throw e;
        }
    }
}
