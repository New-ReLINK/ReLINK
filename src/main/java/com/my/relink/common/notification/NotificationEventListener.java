package com.my.relink.common.notification;

import com.my.relink.domain.notification.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Duration.ofHours(1).toMillis());

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((ex) -> {
            log.info("알림 기능 에러 발생 User Id : {}", userId);
            emitters.remove(userId);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("connect!!!!!!!"));
        } catch (IOException e) {
            log.info("알람 구독 기능 에러 발생 User Id : {}", userId);
            throw new RuntimeException(e);
        }

        emitters.put(userId, emitter);
        return emitter;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotification(NotificationEvent<? extends Notification> event) {
        SseEmitter emitter = emitters.get(event.data().getUserId());

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(event.data().getUserId().toString())
                        .name(event.type().name())
                        .data(event.data())
                        .reconnectTime(Duration.ofHours(1).toMillis())
                );
            } catch (IOException e) {
                log.info("알림 발송 실패 User Id : {}", event.data().getUserId());
                emitters.remove(event.data().getUserId());
            }
        }
    }
}
