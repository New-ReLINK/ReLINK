package com.my.relink.chat.handler;

import com.my.relink.chat.config.ChatPrincipal;
import com.my.relink.config.security.AuthUser;
import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final TradeService tradeService;


    /**
     * 소켓 연결 검증 핸들러
     *
     * - CONNECT: 초기 웹소켓 연결 시 토큰 및 거래 상태 검증
     * - SUBSCRIBE: 특정 채팅방 구독 시 해당 거래 접근 권한 검증
     *
     * @param message
     * @param channel
     * @return
     */

    //TODO jwt 관련 exception 추가되면 에러 catch 추가할 것
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                handleConnect(accessor);
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())){
                handleSubscribe(accessor);
            }
            return message;
        }catch (BusinessException e){
            log.error("웹소켓 연결 검증 중 오류 발생: {}", e.getMessage(), e);
            throw new MessageDeliveryException(e.getMessage());
        }
    }


    /**
     * 초기 웹소켓 연결 시 토큰 및 거래 상태 검증
     *
     * 1. jwt 검증을 통한 사용자 인증
     * 2. 거래 상태 확인
     * 3. 인증된 사용자 정보 Principal로 설정
     * @param accessor
     */
    private void handleConnect(StompHeaderAccessor accessor){
        AuthUser authUser = validateToken(accessor);
        String tradeStatus = accessor.getFirstNativeHeader(WebSocketHeader.TRADE_STATUS_HEADER);
        if(tradeStatus == null){
            throw new BusinessException(ErrorCode.TRADE_STATUS_NOT_FOUND);
        }
        validateTradeStatus(TradeStatus.statusOf(tradeStatus));
        accessor.setUser(new ChatPrincipal(authUser));
    }


    /**
     * 특정 채팅방 구독 시 해당 거래 접근 권한 검증
     *
     * 1. 구독하려는 채팅방의 tradeId 추출
     * 2. 해당 거래의 참여자인지 확인
     * 3. 거래 상태 검증
     * @param accessor
     */
    private void handleSubscribe(StompHeaderAccessor accessor){
        String destination = accessor.getDestination();

        if(destination != null && destination.startsWith("/topic/chats")){
            Long tradeId = extractTradeIdFromPath(destination);
            ChatPrincipal principal = (ChatPrincipal) accessor.getUser();
            Trade trade = tradeService.findByIdOrFail(tradeId);

            trade.validateAccess(principal.getUserId());
            validateTradeStatus(trade.getTradeStatus());
            log.debug("해당 거래 채팅방 접근 검증 완료 - tradeId: {}, userId: {}", tradeId, principal.getUserId());
        }
    }

    /**
     * jwt 검증
     * TODO 추후 jwt 관련 예외 보충 예정
     * @param accessor
     * @return
     */
    private AuthUser validateToken(StompHeaderAccessor accessor){
        String tokenWithPrefix = accessor.getFirstNativeHeader(WebSocketHeader.AUTH_HEADER);
        String token = tokenWithPrefix.replace(JwtProvider.TOKEN_PREFIX, "");
        return jwtProvider.getAuthUserForToken(token);
    }

    /**
     * 거래 상태 검증
     * EXCHANGED나 CANCELED 상태의 거래는 채팅 접근 불가능
     *
     * @param tradeStatus 검증할 거래 상태
     * @throws BusinessException 거래가 이미 종료된 상태(EXCHANGED/CANCELED)인 경우
     */
    private void validateTradeStatus(TradeStatus tradeStatus) {
        if (List.of(TradeStatus.CANCELED, TradeStatus.EXCHANGED).contains(tradeStatus)) {
            log.debug("더 이상 채팅 세션을 제공하지 않는 거래 채팅방에 접근 시도 - tradeStatus : {}", tradeStatus);
            throw new BusinessException(ErrorCode.CHATROOM_ACCESS_DENIED);
        }
    }

    /**
     * STOMP destication 경로에서 tradeId 추출
     * ex) /topic/chats/11 -> 11
     * @param destination
     * @return 추출된 tradeId
     */
    private Long extractTradeIdFromPath(String destination) {
        String[] paths = destination.split("/");
        return Long.parseLong(paths[paths.length - 1]);
    }

}
