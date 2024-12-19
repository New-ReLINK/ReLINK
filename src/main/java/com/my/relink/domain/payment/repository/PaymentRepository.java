package com.my.relink.domain.payment.repository;

import com.my.relink.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentCustomRepository {
}
