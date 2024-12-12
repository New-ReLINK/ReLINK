package com.my.relink.chat.handler;

import com.my.relink.config.security.AuthUser;
import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompHandlerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private JwtProvider jwtProvider;

    private final StompHandler stompHandler;
    private final MessageChannel messageChannel;

    @Autowired
    public StompHandlerTest(StompHandler stompHandler,
                            @Qualifier("clientInboundChannel") MessageChannel messageChannel) { //클라이언트 -> 서버로 들어오는 채널을 확인해야 함
        this.stompHandler = stompHandler;
        this.messageChannel = messageChannel;
    }


    private WebSocketStompClient stompClient;
    private String websocketUrl;

    @BeforeEach
    void setUp() {
        websocketUrl = "http://localhost:" + port + "/chats";
        WebSocketClient webSocketClient = new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))
        );
        stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("유효한 토큰과 진행중인 거래상태로 연결 시도시 성공한다")
    void preSend_connectWithValidToken_andActiveTradeStatus() throws Exception {

        String token = createValidToken();
        StompHeaders headers = createStompHeaders(token, "교환 가능");

        StompSession session = stompClient
                .connectAsync(websocketUrl, new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);


        assertTrue(session.isConnected());

        session.disconnect();
    }


    @Test
    @DisplayName("완료된 거래의 채팅방 접근시 에러 프레임을 생성한다")
    void preSend_connectWithCompletedTradeStatus_createsErrorFrame() {
        StompHeaderAccessor accessor = createStompHeaderAccessor(StompCommand.CONNECT, createValidToken(), TradeStatus.EXCHANGED.getMessage());

        Message<?> connectMessage = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> resultMessage = stompHandler.preSend(connectMessage, messageChannel);

        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(resultMessage, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertEquals(StompCommand.ERROR, resultAccessor.getCommand());
        assertEquals(String.valueOf(ErrorCode.CHATROOM_ACCESS_DENIED.getStatus()),
                resultAccessor.getFirstNativeHeader("status"));
        assertEquals(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage(),
                new String((byte[]) resultMessage.getPayload()));
    }

    @Test
    @DisplayName("취소된 거래의 채팅방 접근시 에러가 발생한다")
    void preSend_connectWithCanceledTradeStatus_createsErrorFrame() {
        StompHeaderAccessor accessor = createStompHeaderAccessor(StompCommand.CONNECT, createValidToken(), TradeStatus.CANCELED.getMessage());

        Message<?> connectMessage = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> resultMessage = stompHandler.preSend(connectMessage, messageChannel);

        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(resultMessage, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertEquals(StompCommand.ERROR, resultAccessor.getCommand());
        assertEquals(String.valueOf(ErrorCode.CHATROOM_ACCESS_DENIED.getStatus()),
                resultAccessor.getFirstNativeHeader("status"));
        assertEquals(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage(),
                new String((byte[]) resultMessage.getPayload()));
    }


    @Test
    @DisplayName("교환 불가능 거래의 채팅방 접근시 에러가 발생한다")
    void preSend_connectWithUNAVAILABLETradeStatus_createsErrorFrame() {
        StompHeaderAccessor accessor = createStompHeaderAccessor(StompCommand.CONNECT, createValidToken(), TradeStatus.UNAVAILABLE.getMessage());

        Message<?> connectMessage = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> resultMessage = stompHandler.preSend(connectMessage, messageChannel);

        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(resultMessage, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertEquals(StompCommand.ERROR, resultAccessor.getCommand());
        assertEquals(String.valueOf(ErrorCode.CHATROOM_ACCESS_DENIED.getStatus()),
                resultAccessor.getFirstNativeHeader("status"));
        assertEquals(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage(),
                new String((byte[]) resultMessage.getPayload()));
    }

    private StompHeaderAccessor createStompHeaderAccessor(StompCommand command, String token, String tradeStatus){
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setNativeHeader(WebSocketHeader.AUTH_HEADER, token);
        accessor.setNativeHeader(WebSocketHeader.TRADE_STATUS_HEADER, tradeStatus);
        return accessor;
    }


    private StompHeaders createStompHeaders(String token, String tradeStatus) {
        StompHeaders headers = new StompHeaders();
        headers.add(WebSocketHeader.AUTH_HEADER, token);
        headers.add(WebSocketHeader.TRADE_STATUS_HEADER, tradeStatus);
        return headers;
    }

    private String createValidToken() {
        User user = User.builder()
                .email("riku1234@naver.com")
                .id(1L)
                .role(Role.USER)
                .build();

        AuthUser authUser = AuthUser.from(user);

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + Role.USER.name())
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                null,
                authorities
        );

        return jwtProvider.generateToken(authentication);
    }


}