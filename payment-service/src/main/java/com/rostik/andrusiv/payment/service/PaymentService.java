package com.rostik.andrusiv.payment.service;


import com.rostik.andrusiv.core.dto.Payment;

import java.util.List;

public interface PaymentService {
    List<Payment> findAll();

    Payment process(Payment payment);
}
