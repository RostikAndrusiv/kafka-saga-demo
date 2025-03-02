package com.rostik.andrusiv.payment.service;

import com.rostik.andrusiv.core.dto.Payment;
import com.rostik.andrusiv.payment.jpa.entity.PaymentEntity;
import com.rostik.andrusiv.payment.jpa.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    public static final String SAMPLE_CREDIT_CARD_NUMBER = "374245455400126";
    private final PaymentRepository paymentRepository;
    private final CreditCardProcessorRemoteService ccpRemoteService;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              CreditCardProcessorRemoteService ccpRemoteService) {
        this.paymentRepository = paymentRepository;
        this.ccpRemoteService = ccpRemoteService;
    }

    @Override
    public Payment process(Payment payment) {
        BigDecimal totalPrice = payment.getProductPrice()
                .multiply(new BigDecimal(payment.getProductQuantity()));

        log.info("Processing payment for Order ID: {}, Product ID: {}, Total Price: {}",
                payment.getOrderId(), payment.getProductId(), totalPrice);


        ccpRemoteService.process(new BigInteger(SAMPLE_CREDIT_CARD_NUMBER), totalPrice);
        log.info("Credit card payment processed successfully for Order ID: {}", payment.getOrderId());

        PaymentEntity paymentEntity = new PaymentEntity();
        BeanUtils.copyProperties(payment, paymentEntity);
        paymentRepository.save(paymentEntity);

        var processedPayment = new Payment();
        BeanUtils.copyProperties(payment, processedPayment);
        processedPayment.setId(paymentEntity.getId());

        log.info("Payment successfully processed and saved for Order ID: {}", payment.getOrderId());
        return processedPayment;
    }

    @Override
    public List<Payment> findAll() {
        log.info("Retrieving all payments from repository.");
        return paymentRepository.findAll().stream().map(entity -> new Payment(entity.getId(), entity.getOrderId(), entity.getProductId(), entity.getProductPrice(), entity.getProductQuantity())
        ).collect(Collectors.toList());
    }
}

