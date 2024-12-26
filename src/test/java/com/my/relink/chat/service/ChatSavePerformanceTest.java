package com.my.relink.chat.service;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.my.relink.chat.config.ChatPrincipal;
import com.my.relink.chat.config.WebSocketConfig;
import com.my.relink.chat.controller.ChatController;
import com.my.relink.chat.controller.dto.request.ChatMessageReqDto;
import com.my.relink.chat.controller.dto.response.ChatMessageRespDto;
import com.my.relink.chat.handler.StompHandler;
import com.my.relink.chat.handler.WebSocketHeader;
import com.my.relink.common.notification.NotificationPublisherService;
import com.my.relink.config.security.AuthUser;
import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.message.repository.MessageRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.service.TradeService;
import com.my.relink.service.UserService;
import jakarta.validation.constraints.Size;
import org.hibernate.internal.build.AllowNonPortable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.client.XhrTransport;

import java.lang.reflect.Type;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.messaging.simp.stomp.StompSession.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ChatSavePerformanceTest {

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private WebSocketConfig webSocketConfig;


    private WebSocketStompClient stompClient;
    private static String WEBSOCKET_URL;
    private static final int MESSAGE_SIZE_LIMIT = 512 * 1024;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<String, StompSession> sessions = new HashMap<>();
    private Map<String, List<Long>> messageLatencies = new ConcurrentHashMap<>();
    private AtomicInteger messageCounter = new AtomicInteger(0);

    private Map<Long, TestUser> connectedUsers = new ConcurrentHashMap<>();

    private int numberOfChatRooms = 100;
    private final List<ChatMessageRespDto> receivedMessages = Collections.synchronizedList(new ArrayList<>());



    @LocalServerPort
    private int port;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ExchangeItemRepository exchangeItemRepository;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    void setUp(){

        User savedOwner = userRepository.save(User.builder()
                .nickname("owner")
                .email("owner@naver.com")
                .password("Owner1234")
                .isDeleted(false)
                .build());

        User savedRequester = userRepository.save(User.builder()
                .nickname("requester..")
                .email("requester@naver.com")
                .password("Requester1234")
                .isDeleted(false)
                .build());

        ExchangeItem exchangeItem = ExchangeItem
                .builder()
                .name("item")
                .user(savedOwner)
                .isDeleted(false)
                .build();
        exchangeItemRepository.save(exchangeItem);

        for(int i = 0;i<numberOfChatRooms; i++){
            tradeRepository.save(Trade.builder()
                            .ownerExchangeItem(exchangeItem)
                            .requesterExchangeItem(null)
                            .requester(savedRequester)
                            .hasRequesterRequested(false)
                            .hasOwnerReceived(false)
                            .hasOwnerRequested(false)
                            .hasRequesterReceived(false)
                            .tradeStatus(TradeStatus.AVAILABLE)
                    .build());
        }


        //웹소켓 client 설정
        WebSocketClient webSocketClient = new StandardWebSocketClient();

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(webSocketClient));

        //SockJS 클라이언트 설정
        SockJsClient sockJsClient = new SockJsClient(transports);

        //하트비트 스케줄러 설정
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("websocket-heartbeat-test");
        taskScheduler.initialize();

        //STOMP 클라이언트 설정
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setTaskScheduler(taskScheduler);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setInboundMessageSizeLimit(1024 * 1024);
        stompClient.setDefaultHeartbeat(new long[]{1000, 1000});

        //웹소켓 전송 설정
        WebSocketTransportRegistration transportRegistration = new WebSocketTransportRegistration();
        transportRegistration
                .setMessageSizeLimit(1024 * 1024)
                .setSendBufferSizeLimit(2048 * 1024)
                .setSendTimeLimit(20 * 1000);


        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setContentLength(MESSAGE_SIZE_LIMIT);


        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    try {
                        new Socket("localhost", port).close();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });

        log.info("서버 준비 완료- port: {}", port);
    }


    @Test
    @DisplayName("100개의 채팅방에서 동시에 메시지 전송 및 저장")
    void 여러_채팅방에서_동시에_메시지_전송_및_저장() throws Exception{

        log.info("WebSocket 연결 시작...");
        WEBSOCKET_URL = String.format("ws://localhost:%d/chats", port);
        log.info("Connecting to: {}", WEBSOCKET_URL);

        int messagePerChat = 50; //채팅방 당 메시지 수 50개로
        Long ownerId = 1L;
        Long requesterId = 2L;

        //채팅방 연결
        for(int i = 0;i<numberOfChatRooms; i++){
            Long tradeId = (long)i;
            connectChatRoom(tradeId, ownerId, requesterId);
            Thread.sleep(200);
        }

        int threadPoolSize = Math.min(numberOfChatRooms * 2, 20); // 50에서 20으로 조정
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        CountDownLatch latch = new CountDownLatch(numberOfChatRooms * 2);
        Thread.sleep(1000);

        long startTime = System.currentTimeMillis();

        for(int i = 0;i<numberOfChatRooms; i++){
            Long tradeId = (long)i;
            executorService.submit(() -> {
                try{
                    simulateChat(ownerId, tradeId, messagePerChat/2);
                } finally {
                    latch.countDown();
                }
            });

            executorService.submit(() -> {
                try{
                    simulateChat(requesterId, tradeId, messagePerChat/2);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(100, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        if(!completed){
            log.warn("시간 내에 모든 채팅이 이루어지지 않음..");
        }

        printTestResults(startTime, endTime, numberOfChatRooms, messagePerChat);
        executorService.shutdown();
        connectedUsers.values().forEach(user -> {
            if(user.session.isConnected()){
                user.session.disconnect();
            }
        });

        assertAll(
                () -> assertTrue(messageCounter.get() >= messagePerChat * numberOfChatRooms * 0.8,
                        "최소 80% 이상의 메시지가 전송되어야 합니다"),
                () -> assertEquals(messageCounter.get(), receivedMessages.size(),
                        "전송된 메시지 수와 저장된 메시지 수가 일치해야 합니다"),
                () -> {
                    // 각 채팅방별 메시지 수 확인
                    Map<Long, Long> messagesPerTrade = receivedMessages.stream()
                            .collect(Collectors.groupingBy(
                                    ChatMessageRespDto::getTradeId,
                                    Collectors.counting()
                            ));

                    assertTrue(messagesPerTrade.values().stream()
                                    .allMatch(count -> count == messagePerChat),
                            "채팅방 별 메시지 수가 일치하지 않습니다");
                }
        );

    }

    private void connectChatRoom(Long tradeId, Long ownerId, Long requesterId) throws ExecutionException, InterruptedException, TimeoutException {
        StompHeaders ownerConnectionHeaders = createConnectHeaders(tradeId, createValidToken(ownerId));
        CompletableFuture<StompSession> ownerFuture = stompClient.connectAsync(
                WEBSOCKET_URL,
                new WebSocketHttpHeaders(), //핸드솈 헤더
                ownerConnectionHeaders, //stomp 연결 헤더
                new AuthenticatedStompSessionHandler("owner", tradeId),
                new Object[]{}
        );
        StompSession ownerSession = ownerFuture.get(30, TimeUnit.SECONDS); //연결 타임아웃 30초로 설정
        connectedUsers.put(ownerId, new TestUser(ownerId, "owner", ownerSession));

        StompHeaders requesterConnectHeaders = createConnectHeaders(tradeId, createValidToken(requesterId));
        CompletableFuture<StompSession> requesterFuture = stompClient.connectAsync(
                WEBSOCKET_URL,
                new WebSocketHttpHeaders(),
                requesterConnectHeaders,
                new AuthenticatedStompSessionHandler("requester", tradeId),
                new Object[]{}
        );

        StompSession requesterSession = requesterFuture.get(30, TimeUnit.SECONDS); //연결 타임아웃 30초로 설정
        connectedUsers.put(requesterId, new TestUser(requesterId, "requester", requesterSession));

        // 채팅방 구독 설정
        subscribeToChat(ownerSession, requesterSession, tradeId);
    }

    private void subscribeToChat(StompSession ownerSession, StompSession requestSession, Long tradeId){
        String destination = "/topic/chats/" + tradeId;
        StompFrameHandler frameHandler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageRespDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                ChatMessageRespDto message = (ChatMessageRespDto) payload;
                receivedMessages.add(message);
                LocalDateTime sentDateTime = LocalDateTime.parse(message.getSentAt());
                long sentMillisTime = sentDateTime.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();


                long endTime = System.currentTimeMillis();
                messageLatencies.computeIfAbsent(message.getSenderId().toString(), k ->
                        new ArrayList<>()).add(endTime - sentMillisTime);
            }
        };

        ownerSession.subscribe(destination, frameHandler);
        requestSession.subscribe(destination, frameHandler);
    }


    private void simulateChat(Long userId, Long tradeId, int messageCount) {
        TestUser user = connectedUsers.get(userId);
        if(user == null || !user.getSession().isConnected()){
            log.error("유저Id {} 연결 세션 없음", userId);
            return;
        }

        Random random = new Random();
        int retryCount = 3;

        for(int i = 0;i<messageCount; i++){
            boolean messageSent = false;
            int attempts = 0;

            while(!messageSent && attempts < retryCount){
                try{
                    if (!user.getSession().isConnected()) {
                        log.error("세션 연결 끊김 - userId: {}, messageCount: {}/{}",
                                userId, i, messageCount);
                        //재연결 시도
                        reconnectUser(userId, tradeId);
                        user = connectedUsers.get(userId);
                        if (user == null) {
                            throw new IllegalStateException("재연결 실패");
                        }
                    }
                    ChatMessageReqDto message = new ChatMessageReqDto(generateMessageContent(i, user.getUserType()), tradeId);

                    StompSession session = user.getSession();
                    if(session != null && session.isConnected()){
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            session.send("/app/chats/" + tradeId + "/message", message);
                        });

                        future.get(3, TimeUnit.SECONDS); //타임아웃으로 TEXT_PARTIAL_WRITING 상태 방지
                        messageSent = true;
                        messageCounter.incrementAndGet();

                        Thread.sleep(100 + random.nextInt(50));
                    }
                } catch (TimeoutException e) {
                    attempts++;
                    log.warn("메시지 전송 타임아웃 - userId: {}, messageIdx: {}", userId, i);
                } catch (Exception e){
                    attempts++;
                    if(attempts >= retryCount){
                        log.error("메시지 전송 실패 - userId: {}, messageIdx: {}, attempts: {}",
                                userId, i, attempts, e);
                    } else {
                        log.warn("메시지 전송 재시도 - userId: {}, messageIdx: {}, attempt: {}",
                                userId, i, attempts);
                        try {
                            Thread.sleep(1000); // 재시도 전 대기
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void reconnectUser(Long userId, Long tradeId) {
        try {
            TestUser oldUser = connectedUsers.get(userId);
            String userType = oldUser.getUserType();

            // 이전 세션 정리
            if (oldUser.session != null && oldUser.session.isConnected()) {
                oldUser.session.disconnect();
            }

            Thread.sleep(1000);

            StompHeaders headers = createConnectHeaders(tradeId, createValidToken(userId));
            CompletableFuture<StompSession> future = stompClient.connectAsync(
                    WEBSOCKET_URL,
                    new WebSocketHttpHeaders(),
                    headers,
                    new AuthenticatedStompSessionHandler(userType, tradeId),
                    new Object[]{}
            );

            StompSession newSession = future.get(15, TimeUnit.SECONDS); //재연결시 타임아웃 15초
            connectedUsers.put(userId, new TestUser(userId, userType, newSession));

            //채팅방 재구독
            subscribeToChat(newSession, tradeId);

            Thread.sleep(1000); //잠시 대기


            log.info("사용자 재연결 성공 - userId: {}", userId);
        } catch (Exception e) {
            log.error("사용자 재연결 실패 - userId: {}", userId, e);
        }
    }

    private void subscribeToChat(StompSession session, Long tradeId) {
        String destination = "/topic/chats/" + tradeId;
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageRespDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                ChatMessageRespDto message = (ChatMessageRespDto) payload;
                long endTime = System.currentTimeMillis();
                LocalDateTime sentDateTime = LocalDateTime.parse(message.getSentAt());

                long sentAtMillis = sentDateTime.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();

                messageLatencies.computeIfAbsent(
                        message.getSenderId().toString(),
                        k -> new ArrayList<>()
                ).add(endTime - sentAtMillis);
                messageCounter.incrementAndGet();
            }
        });
    }


    private void printTestResults(long startTime, long endTime, int numberOfChatRooms, int messagesPerChat){
        double totalTimeSeconds = (endTime - startTime) / 1000.0;
        int totalMessages = numberOfChatRooms * messagesPerChat;
        double messagesPerSecond = totalMessages / totalTimeSeconds;

        List<Long> allLatencies = messageLatencies.values().stream()
                .flatMap(List::stream)
                .sorted()
                .collect(Collectors.toList());
        if (!allLatencies.isEmpty()) {
            double avgLatency = allLatencies.stream().mapToLong(l -> l).average().orElse(0);
            long p95Latency = allLatencies.get((int)(allLatencies.size() * 0.95));
            long p99Latency = allLatencies.get((int)(allLatencies.size() * 0.99));

            log.info("\n=== Chat Performance Test Results ===");
            log.info("Number of Chat Rooms: {}", numberOfChatRooms);
            log.info("Messages per Chat Room: {}", messagesPerChat);
            log.info("Total Messages: {}", totalMessages);
            log.info("Total Time: {} seconds", totalTimeSeconds);
            log.info("Messages per Second: {}", messagesPerSecond);
            log.info("Average Latency: {} ms", avgLatency);
            log.info("95th Percentile Latency: {} ms", p95Latency);
            log.info("99th Percentile Latency: {} ms", p99Latency);
            log.info("Successfully Processed Messages: {}", messageCounter.get());
            log.info("Message Success Rate: {}%",
                    (messageCounter.get() * 100.0 / totalMessages));
        }

    }

    private String createValidToken(Long userId) {
        User user = User.builder()
                .email("user"+userId+"@naver.com")
                .id(userId)
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

    private String generateMessageContent(int index, String userType) {
        return String.format("Message %d from %s: %s",
                index, userType, UUID.randomUUID().toString());
    }


    private  class AuthenticatedStompSessionHandler extends StompSessionHandlerAdapter{
        private final String userType;
        private final Long tradeId;
        public AuthenticatedStompSessionHandler(String userType, Long tradeId) {
            this.userType = userType;
            this.tradeId = tradeId;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            log.debug("{} 연결: 거래 id {}", userType, tradeId);
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            log.error("{} Connection Error for trade {}: ", userType, tradeId, exception);
        }
    }


    private class TestUser {
        private Long userId;
        private String userType;
        private StompSession session;

        public TestUser(Long userId, String userType, StompSession session) {
            this.userId = userId;
            this.userType = userType;
            this.session = session;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUserType() {
            return userType;
        }

        public StompSession getSession() {
            return session;
        }
    }



    private StompHeaders createConnectHeaders(Long tradeId, String token) {
        StompHeaders headers = new StompHeaders();
        headers.add(WebSocketHeader.AUTH_HEADER, "Bearer "+token);
        headers.add(WebSocketHeader.TRADE_STATUS_HEADER, TradeStatus.AVAILABLE.getMessage());
        return headers;
    }


}
