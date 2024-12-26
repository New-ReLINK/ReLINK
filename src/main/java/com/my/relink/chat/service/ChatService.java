package com.my.relink.chat.service;

import com.my.relink.chat.controller.dto.request.ChatImageReqDto;
import com.my.relink.chat.controller.dto.request.ChatMessageReqDto;
import com.my.relink.chat.controller.dto.response.ChatImageRespDto;
import com.my.relink.chat.controller.dto.response.ChatMessageRespDto;
import com.my.relink.common.notification.NotificationPublisherService;
import com.my.relink.config.s3.S3Service;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.repository.MessageRepository;
import com.my.relink.domain.notification.chat.ChatStatus;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.service.TradeService;
import com.my.relink.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatService {

    private final MessageRepository messageRepository;
    private final TradeService tradeService;
    private final UserService userService;
    private final S3Service s3Service;
    private final ImageRepository imageRepository;
    private final NotificationPublisherService notificationPublisherService;

    @Transactional
    public ChatImageRespDto saveImageForChat(Long tradeId, ChatImageReqDto chatImageReqDto) {
        String uploadedImageUrl = null;
        try {
            Trade trade = tradeService.findByIdOrFail(tradeId);
            uploadedImageUrl = s3Service.upload(chatImageReqDto.getImage());
            Image image = imageRepository.save(Image.builder()
                    .imageUrl(uploadedImageUrl)
                    .entityType(EntityType.TRADE)
                    .entityId(trade.getId())
                    .build());
            return new ChatImageRespDto(image);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            log.error("[채팅 이미지 저장 실패] tradeId = {}, cause = {}", tradeId, e.getMessage(), e);
            handleImageFail(uploadedImageUrl, tradeId);
            throw new BusinessException(ErrorCode.FAIL_TO_SAVE_IMAGE);
        }
    }

    private void handleImageFail(String uploadedImageUrl, Long tradeId) {
        if (uploadedImageUrl != null) {
            try {
                s3Service.deleteImage(uploadedImageUrl);
            } catch (Exception deleteFail) {
                log.error("[이미지 삭제 실패] tradeId = {}, imageUrl = {}, cause = {}",
                        tradeId, uploadedImageUrl, deleteFail.getMessage(), deleteFail);
            }
        }
    }

    @Transactional
    public ChatMessageRespDto saveMessage(Long tradeId, ChatMessageReqDto chatMessageReqDto, Long senderId) {
        User sender = userService.findByIdOrFail(senderId);
        Trade trade = tradeService.findByIdWithOwnerItemOrFail(tradeId);
        Message message = messageRepository.save(chatMessageReqDto.toEntity(trade, sender));
        notificationPublisherService.crateChatNotification(
                senderId,
                message.getContent(),
                sender.getNickname(),
                trade.getOwnerExchangeItem().getName(),
                ChatStatus.NEW_CHAT
        );
        return new ChatMessageRespDto(message);
    }

    public void deleteChatsByTradeId(Long tradeId) {
        messageRepository.deleteMessage(tradeId);
    }

}
