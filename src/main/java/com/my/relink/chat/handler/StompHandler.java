package com.my.relink.chat.handler;

import com.my.relink.chat.config.ChatPrincipal;
import com.my.relink.config.security.AuthUser;
import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
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


    /**
     * 초기 웹소켓 연결 시점에 검증 진행
     *
     * 이미 '/chat/{tradeId}' 시점에서 해당 trade에 접근 가능한지 검사하기 때문에
     * 여기서는 토큰과 채팅 세션 연결 검증만 수행한다
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
                //토큰 검증
                String tokenWithPrefix = accessor.getFirstNativeHeader(WebSocketHeader.AUTH_HEADER);
                String token = tokenWithPrefix.replace(JwtProvider.TOKEN_PREFIX, "");
                AuthUser authUser = jwtProvider.getAuthUserForToken(token);

                //TradeStatus 검증: EXCHANGED, CANCELED가 아닐 때만 접근 허용한다
                validateChatRoomAccess(accessor);
                accessor.setUser(new ChatPrincipal(authUser));
            }
            return message;
        }catch (BusinessException e){
            log.error("웹소켓 연결 검증 중 오류 발생: {}", e.getMessage(), e);
            throw new MessageDeliveryException(e.getMessage());
        }
    }

    private void validateChatRoomAccess(StompHeaderAccessor accessor){
        String tradeStatus = accessor.getFirstNativeHeader(WebSocketHeader.TRADE_STATUS_HEADER);

        if(tradeStatus == null){
            throw new BusinessException(ErrorCode.TRADE_STATUS_NOT_FOUND);
        }

        if(List.of(TradeStatus.CANCELED, TradeStatus.EXCHANGED).contains(TradeStatus.statusOf(tradeStatus))){
            log.debug("더 이상 채팅 세션을 제공하지 않는 거래 채팅방에 접근 시도 - tradeStatus : {}", tradeStatus);
            throw new BusinessException(ErrorCode.CHATROOM_ACCESS_DENIED);
        }
    }

    private Message<byte[]> createErrorMessage(String errorMessage) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setMessage(errorMessage);
        errorAccessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                new byte[0],
                errorAccessor.getMessageHeaders()
        );
    }
}
