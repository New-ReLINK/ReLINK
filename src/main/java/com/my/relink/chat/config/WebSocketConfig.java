package com.my.relink.chat.config;

import com.my.relink.chat.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker //STOMP 활성화
@RequiredArgsConstructor
@Slf4j
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

    /**백업 통신 방법 설정
     *
     * 웹소켓 연결에 실패했을 시 SockJS가 대체 전송 방식을 사용하며, 이 떄 해당 설정이 적용된다
     *
     * 웹소켓 엔드포인트 등록 및 모든 오리진에서의 접근 허용
     *
     * ex) ws:localhost:9090/chats
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setStreamBytesLimit(512 * 1024) //512kb
                .setHttpMessageCacheSize(1000) //웹소켓 연결이 끊겼을 시 재연결을 위해 메시지를 캐시하는 개수. 1000개 정도가 일시적 네트워크 문제 시 대부분의 메시지를 복구할 수 있다 함
                .setDisconnectDelay(30 * 1000); //30초. 클라이언트와의 연결이 끊어졌다고 판단하기까지의 대기 시간
    }

    /**
     * 주 통신 방법 설정
     *
     * 웹소켓 연결 성공시 해당 설정이 적용된다
     *
     * @param registry
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry
                .setMessageSizeLimit(512 * 1024)  // 메시지 크기 제한: 512kb
                .setSendBufferSizeLimit(1024 * 1024)  // 버퍼 크기 제한: 1mb
                .setSendTimeLimit(20 * 1000)  // 메시지 전송 제한 시간: 20초
                .setTimeToFirstMessage(30 * 1000);  // 첫 메시지 수신 대기 시간: 30초
    }

    /**
     * 클라이언트로부터 들어오는 메시지를 처리하는 채널에 인터셉터 등록
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
        log.debug("stomp handler 인터셉터 등록 완료");
    }
}
