package com.my.relink.domain.payment.repository;

import com.my.relink.controller.point.dto.response.PointChargeHistoryRespDto;
import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.PaymentType;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageResponse;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class PaymentCustomRepositoryImplTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Test
    @DisplayName("포인트 충전 내역 조회에 성공한다")
    void getPointChargeHistories_success(){
        User user = createAndSaveUser();
        int page = 0;
        int size = 5;

        IntStream.range(0, 10).forEach(i -> {
            Payment payment = Payment.builder()
                    .user(user)
                    .paymentType(PaymentType.POINT_CHARGE)
                    .method("신용카드")
                    .status("DONE")
                    .amount(1000000)
                    .paidAt(LocalDateTime.now().minusDays(1))
                    .build();
            paymentRepository.save(payment);
        });

        Payment shouldNotBeInclude1 = Payment.builder()
                .user(user)
                .paymentType(null)
                .amount(5000)
                .status("DONE")
                .paidAt(LocalDateTime.now())
                .build();
        Payment shouldNotBeInclude2 = Payment.builder()
                .user(user)
                .paymentType(null)
                .amount(3000)
                .status("DONE")
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(shouldNotBeInclude1);
        paymentRepository.save(shouldNotBeInclude2);


        PageResponse<PointChargeHistoryRespDto> result = paymentRepository.findPointChargeHistories(user, page, size);

        assertAll(() -> {
            assertEquals(result.getContent().size(), 5);
            assertEquals(result.getPageInfo().getTotalCount(), 10);
            assertEquals(result.getPageInfo().getTotalPages(), 2);
            assertTrue(result.getPageInfo().isHasNext());
            assertFalse(result.getPageInfo().isHasPrevious());
            assertThat(result.getContent())
                    .isSortedAccordingTo((a, b) -> b.getChargedDateTime().compareTo(a.getChargedDateTime()));
        });
    }


    private User createAndSaveUser() {
        User user = User.builder()
                .email("riku1234@naver.com")
                .password("password")
                .nickname("maeda")
                .build();
        return userRepository.save(user);
    }

}