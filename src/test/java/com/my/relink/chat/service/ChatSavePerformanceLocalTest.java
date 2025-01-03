package com.my.relink.chat.service;

import com.my.relink.chat.aop.metric.OperationMetrics;
import com.my.relink.chat.config.WebSocketConfig;
import com.my.relink.chat.config.WebSocketMetricsCollector;
import com.my.relink.chat.config.metric.WebSocketPerformanceMetrics;
import com.my.relink.chat.controller.dto.request.ChatMessageReqDto;
import com.my.relink.chat.controller.dto.response.ChatMessageRespDto;
import com.my.relink.chat.handler.StompHandler;
import com.my.relink.chat.handler.WebSocketHeader;
import com.my.relink.config.cache.RedisConfig;
import com.my.relink.config.security.AuthUser;
import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.message.repository.MessageRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.yaml.snakeyaml.util.Tuple;

import java.lang.reflect.Type;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.my.relink.chat.handler.StompHandler.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@EnableScheduling
public class ChatSavePerformanceLocalTest {

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private WebSocketConfig webSocketConfig;

    @Autowired
    private StompHandler stompHandler;

    private WebSocketStompClient stompClient;
    private static String WEBSOCKET_URL;
    private static final int MESSAGE_SIZE_LIMIT = 512 * 1024;
    private static final String CHAT_TRADE_PREFIX = "chat:trade:*";


    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<String, StompSession> sessions = new HashMap<>();
    private Map<String, List<Long>> messageLatencies = new ConcurrentHashMap<>();
    private AtomicInteger messageCounter = new AtomicInteger(0);

    private Map<String, TestUser> connectedUsers = new ConcurrentHashMap<>();

    private int numberOfChatRooms = 100;

    private final Set<ChatMessageRespDto> receivedMessages = ConcurrentHashMap.newKeySet();


    @LocalServerPort
    private int port;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ExchangeItemRepository exchangeItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private WebSocketMetricsCollector metricsCollector;

    @Autowired
    private OperationMetrics serviceMetric;
    private final WebSocketPerformanceMetrics metrics = new WebSocketPerformanceMetrics();



    @BeforeEach
    void setUp(){

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
        stompClient.setDefaultHeartbeat(new long[]{10000, 10000});

        //웹소켓 전송 설정
        WebSocketTransportRegistration transportRegistration = new WebSocketTransportRegistration();
        transportRegistration
                .setMessageSizeLimit(512 * 1024)
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

    private Trade getParticipant(Long tradeId){
        return tradeRepository.findByIdWithItemsAndUser(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));
    }

    @Test
    @DisplayName("500개의 채팅방에서 동시에 연결 및 메시지 전송")
    void 여러_채팅방에서_동시에_연결_및_메시지_전송() throws Exception {
        metrics.startMeasurement();
        log.info("WebSocket 연결 시작...");
        WEBSOCKET_URL = String.format("ws://localhost:%d/chats", port);
        log.info("Connecting to: {}", WEBSOCKET_URL);

        numberOfChatRooms = 100;
        int messagePerChat = 100;

//        numberOfChatRooms = 500;
//        int messagePerChat = 100;

//
//        numberOfChatRooms = 500;
//        int messagePerChat = 200;

        // 전체 작업을 관리할 스레드 풀
        int threadPoolSize = (int) (200 * 0.35); //50으로 바꿀것..
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 전체 작업 완료를 기다리는 latch (연결 + 채팅)
        CountDownLatch completionLatch = new CountDownLatch(numberOfChatRooms * 2);

        long startTime = System.currentTimeMillis();

        // 각 채팅방의 owner와 requester에 대해 연결과 채팅을 하나의 작업으로
        for (int i = 1; i <= numberOfChatRooms; i++) {
            final Long tradeId = (long) i;
            Trade trade = getParticipant(tradeId);

            // owner 연결 및 채팅
            executorService.submit(() -> {
                try {
                    // 1. owner 연결
                    connectUser(tradeId, trade.getOwner(), "owner");
                    // 2. 채팅 시작
                    simulateChat("owner", tradeId, messagePerChat/2);
                } catch (Exception e) {
                    log.error("Owner 작업 실패 - tradeId: {}", tradeId, e);
                } finally {
                    completionLatch.countDown();
                }
            });

            // requester 연결 및 채팅
            executorService.submit(() -> {
                try {
                    // 1. requester 연결
                    connectUser(tradeId, trade.getRequester(), "requester");
                    // 2. 채팅 시작
                    simulateChat("requester", tradeId, messagePerChat/2);
                } catch (Exception e) {
                    log.error("Requester 작업 실패 - tradeId: {}", tradeId, e);
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // 모든 작업 완료 대기
        //boolean completed = completionLatch.await(120, TimeUnit.SECONDS); //채팅방 100개, 각각 50건의 대화

        boolean completed = completionLatch.await(600, TimeUnit.SECONDS);  // 10분. 채팅방 500개, 각각 100건의 대화
        //boolean completed = completionLatch.await(900, TimeUnit.SECONDS);  // 15분. 채팅방 500개, 각각 200건의 대화

        long endTime = System.currentTimeMillis();

        if (!completed) {
            log.warn("시간 내에 모든 작업이 완료되지 않았습니다.");
        }

        executorService.shutdown();
        // ExecutorService 먼저 종료 대기
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        connectedUsers.values().forEach(user -> {
            if(user.session.isConnected()) {
                try {
                    user.session.disconnect();
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("Disconnect error for session: {}", user.session.getSessionId(), e);
                }
            }
        });

        // 결과 검증 및 정리
        long actualDbCount = messageRepository.count();
        metrics.endMeasurement();
        metrics.printResults(numberOfChatRooms, messagePerChat, actualDbCount);

        //전체 성능 지표
        printTestResults(startTime, endTime, numberOfChatRooms, messagePerChat, actualDbCount);

        log.info("전송 시도된 메시지: {}", messageCounter.get());
        log.info("DB 저장된 메시지: {}", actualDbCount);
        log.info("수신된 메시지: {}", receivedMessages.size());

        assertAll(
                () -> assertTrue(messageCounter.get() >= messagePerChat * numberOfChatRooms * 0.8,
                        "최소 80% 이상의 메시지가 전송되어야 합니다"),
                () -> assertEquals(messageCounter.get(), actualDbCount,
                        "전송된 메시지 수와 DB 저장 메시지 수가 일치해야 합니다")

        );

    }


    private void connectUser(Long tradeId, User user, String userType) throws Exception {
        CountDownLatch connectLatch = new CountDownLatch(1);
        CountDownLatch subscribeLatch = new CountDownLatch(1);

        StompSession session = connectStompClient(tradeId, user, userType, connectLatch);
        if (!connectLatch.await(10, TimeUnit.SECONDS)) {
            throw new TimeoutException("STOMP 연결 타임아웃");
        }

        log.info("채팅방 연결: tradeId = {}, userId = {}", tradeId, user.getId());
        connectedUsers.put(tradeId+"_"+userType, new TestUser(user.getId(), userType,  session));

        subscribeToChat(session, tradeId, subscribeLatch);
        if (!subscribeLatch.await(15, TimeUnit.SECONDS)) {
            throw new TimeoutException("STOMP 구독 타임아웃");
        }
    }


    private StompSession connectStompClient(Long tradeId, User user, String userType, CountDownLatch connectLatch) {
        try {
            StompHeaders connectHeaders = createConnectHeaders(tradeId, createValidToken(user));
            StompSessionHandler sessionHandler = new AuthenticatedStompSessionHandler(userType, tradeId, connectLatch);

            CompletableFuture<StompSession> future = stompClient.connectAsync(
                    WEBSOCKET_URL,
                    new WebSocketHttpHeaders(),
                    connectHeaders,
                    sessionHandler,
                    new Object[]{}
            );

            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("STOMP 연결 실패 - userType: {}, tradeId: {}", userType, tradeId, e);
            throw new RuntimeException("STOMP 연결 실패", e);
        }
    }


    private class CustomStompFrameHandler implements StompFrameHandler {
        private final CountDownLatch subscribeLatch;

        public CustomStompFrameHandler(CountDownLatch subscribeLatch) {
            this.subscribeLatch = subscribeLatch;
        }

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
            subscribeLatch.countDown();
        }
    }


    private void simulateChat(String userType, Long tradeId, int messageCount) {
        TestUser user = connectedUsers.get(tradeId+"_"+userType);

        if(user == null) {
            log.error("유저 정보를 찾을 수 없음 - tradeId: {}, userType: {}", tradeId, userType);
            return;
        }

        if(!user.getSession().isConnected()) {
            log.error("유저Id {} 연결 세션 없음", user.getUserId());
            return;
        }

        Random random = new Random();
        int retryCount = 3;

        for(int i = 0;i<messageCount; i++){
            boolean messageSent = false;
            int attempts = 0;
            boolean countIncremented = false;

            while(!messageSent && attempts < retryCount){
                try{
                    if (!user.getSession().isConnected()) {
                        log.error("세션 연결 끊김 - userId: {}, messageCount: {}/{}",
                                user.getUserId(), i, messageCount);
                        //재연결 시도
                        reconnectUser(user.getUserId(), tradeId);
                        user = connectedUsers.get(new Tuple<>(tradeId, userType));
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

                        try {
                            future.get(3, TimeUnit.SECONDS); //타임아웃으로 TEXT_PARTIAL_WRITING 상태 방지
                            messageSent = true;
                            if(!countIncremented) {
                                messageCounter.incrementAndGet();
                                countIncremented = true;
                            }
                        }catch (TimeoutException e){
                            future.cancel(true);
                            attempts++;
                            log.warn("메시지 전송 타임아웃 - userId: {}, messageIdx: {}", user.getUserId(), i);
                        }

                        //Thread.sleep(100 + random.nextInt(50)); //채팅방 100, 채팅방 당 메시지 50건
                        Thread.sleep(150 + random.nextInt(50)); //채팅방 500, 채팅방 당 메시지 100
                    }
                } catch (Exception e){
                    attempts++;
                    if(attempts >= retryCount){
                        log.error("메시지 전송 실패 - userId: {}, messageIdx: {}, attempts: {}",
                                user.getUserId(), i, attempts, e);
                    } else {
                        log.warn("메시지 전송 재시도 - userId: {}, messageIdx: {}, attempt: {}",
                                user.getUserId(), i, attempts);
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

            CountDownLatch connectLatch = new CountDownLatch(1);
            CountDownLatch subscribeLatch = new CountDownLatch(1);

            StompHeaders headers = createConnectHeaders(tradeId, createValidToken(userId));
            CompletableFuture<StompSession> future = stompClient.connectAsync(
                    WEBSOCKET_URL,
                    new WebSocketHttpHeaders(),
                    headers,
                    new AuthenticatedStompSessionHandler(userType, tradeId, connectLatch),
                    new Object[]{}
            );

            StompSession newSession = future.get(15, TimeUnit.SECONDS); //재연결시 타임아웃 15초

            // 연결 완료 대기
            if (!connectLatch.await(15, TimeUnit.SECONDS)) {
                throw new TimeoutException("STOMP 재연결 타임아웃");
            }

            connectedUsers.put(tradeId+"_"+userType, new TestUser(userId, userType, newSession));

            //채팅방 재구독
            subscribeToChat(newSession, tradeId, subscribeLatch);

            Thread.sleep(1000); //잠시 대기


            log.info("사용자 재연결 성공 - userId: {}", userId);
        } catch (Exception e) {
            log.error("사용자 재연결 실패 - userId: {}", userId, e);
        }
    }

    private void subscribeToChat(StompSession session, Long tradeId, CountDownLatch latch) {
        String destination = "/topic/chats/" + tradeId;
        StompFrameHandler frameHandler = new CustomStompFrameHandler(latch);

        try {
            StompSession.Subscription subscription = session.subscribe(destination, frameHandler);
            latch.countDown();
        } catch (Exception e) {
            log.error("구독 실패 - tradeId: {}", tradeId, e);
            throw new RuntimeException("구독 실패", e);
        }

    }


    private void printTestResults(long startTime, long endTime, int numberOfChatRooms, int messagesPerChat, long actualDBCount){
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
            log.info("Successfully Processed Messages: {}", actualDBCount);
            log.info("Message Success Rate: {}%",
                    (actualDBCount * 100.0 / totalMessages));
        }

        log.info("\n=== StompHandler Performance ===");
        Map<String, MetricsDTO> metrics = stompHandler.getMetrics();

        metrics.forEach((operation, metric) -> {
            log.info("{} Operation:", operation);
            log.info("  Count: {}", metric.getCount());
            log.info("  Average: {}ms", metric.getAverageLatency());
            log.info("  Max: {}ms", metric.getMaxLatency());
            log.info("  95th percentile: {}ms", metric.getPercentile95());
        });

        serviceMetric.printMetrics();

    }

    public static class RedisRoomMetrics {
        final long messageCount;
        final long ttl;
        final LocalDateTime firstMessageTime;
        final LocalDateTime lastMessageTime;

        public RedisRoomMetrics(long messageCount, long ttl, LocalDateTime firstMessageTime, LocalDateTime lastMessageTime) {
            this.messageCount = messageCount;
            this.ttl = ttl;
            this.firstMessageTime = firstMessageTime;
            this.lastMessageTime = lastMessageTime;
        }
    }



    private String createValidToken(User user) {
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
        private final CountDownLatch connectLatch;

        public AuthenticatedStompSessionHandler(String userType, Long tradeId, CountDownLatch connectLatch) {
            this.userType = userType;
            this.tradeId = tradeId;
            this.connectLatch = connectLatch;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            log.info("STOMP 연결 성공 - userType: {}, tradeId: {}, sessionId: {}",
                    userType, tradeId, session.getSessionId());
            connectLatch.countDown();
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            log.error("STOMP 전송 에러 - userType: {}, tradeId: {}, sessionId: {}",
                    userType, tradeId, session.getSessionId(), exception);        }
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
