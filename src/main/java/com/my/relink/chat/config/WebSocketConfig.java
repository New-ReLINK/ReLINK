package com.my.relink.chat.config;

import com.my.relink.chat.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker //STOMP 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    /**
     * 메시지 브로커 설정
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); //구독 요청을 처리할 prefix
        registry.setApplicationDestinationPrefixes("/app"); //클라이언트가 메시지를 발행할 때 사용할 prefix
    }

    /**
     * 웹소켓 엔드포인트 등록 및 모든 오리진에서의 접근 허용
     *
     * ex) ws:localhost:9090/chats
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chats")
                .setAllowedOrigins("*")
                .withSockJS();
    }


    /**
     * 클라이언트로부터 들어오는 메시지를 처리하는 채널에 인터셉터 등록
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
