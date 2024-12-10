package com.my.relink.chat.handler;

import com.my.relink.config.security.AuthUser;
import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompHandlerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private JwtProvider jwtProvider;


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
    @DisplayName("완료된 거래의 채팅방 접근시 에러가 발생한다")
    void preSend_connectWithCompletedTradeStatus_throwsException() {
        String token = createValidToken();
        StompHeaders headers = createStompHeaders(token, "교환 완료");

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            stompClient
                    .connectAsync(websocketUrl, new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {
                        @Override
                        public void handleTransportError(StompSession session, Throwable exception) {
                            if (exception.getCause() instanceof MessageDeliveryException) {
                                MessageDeliveryException ex = (MessageDeliveryException) exception.getCause();
                                assertEquals(ErrorCode.TRADE_ACCESS_DENIED.getMessage(), ex.getMessage());
                            }
                        }
                    })
                    .get(3, TimeUnit.SECONDS);
        });

        Throwable cause = exception.getCause();

        assertTrue(cause instanceof ConnectionLostException);
    }

    @Test
    @DisplayName("취소된 거래의 채팅방 접근시 에러가 발생한다")
    void preSend_connectWithCanceledTradeStatus_throwsException() {
        String token = createValidToken();
        StompHeaders headers = createStompHeaders(token, "교환 취소");

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            stompClient
                    .connectAsync(websocketUrl, new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {
                        @Override
                        public void handleTransportError(StompSession session, Throwable exception) {
                            if (exception.getCause() instanceof MessageDeliveryException) {
                                MessageDeliveryException ex = (MessageDeliveryException) exception.getCause();
                                assertEquals(ErrorCode.TRADE_ACCESS_DENIED.getMessage(), ex.getMessage());
                            }
                        }
                    })
                    .get(3, TimeUnit.SECONDS);
        });

        Throwable cause = exception.getCause();

        assertTrue(cause instanceof ConnectionLostException);
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