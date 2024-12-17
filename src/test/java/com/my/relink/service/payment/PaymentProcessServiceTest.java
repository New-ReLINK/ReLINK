package com.my.relink.service.payment;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessServiceTest {

    @InjectMocks
    private PaymentProcessService paymentProcessService;

    @Mock
    private PaymentService paymentService;


    

}