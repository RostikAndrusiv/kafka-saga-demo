package com.rostik.andrusiv.payment.service;

import com.rostik.andrusiv.core.dto.CreditCardProcessRequest;
import com.rostik.andrusiv.core.exception.CreditCardProcessorUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
public class CreditCardProcessorRemoteServiceImpl implements CreditCardProcessorRemoteService {
    private final RestTemplate restTemplate;
    private final String ccpRemoteServiceUrl;

    public CreditCardProcessorRemoteServiceImpl(
            RestTemplate restTemplate,
            @Value("${remote.ccp.service.name}") String ccpRemoteServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.ccpRemoteServiceUrl = ccpRemoteServiceUrl;
    }

    @Override
    public void process(BigInteger cardNumber, BigDecimal paymentAmount) {
        log.info("Processing payment for card number: {}, Amount: {}", cardNumber, paymentAmount);

        try {
            var request = new CreditCardProcessRequest(cardNumber, paymentAmount);
            log.info("Sending request to remote service at: {} with card number: {} and amount: {}",
                    ccpRemoteServiceUrl, cardNumber, paymentAmount);

            restTemplate.postForObject("http://" + ccpRemoteServiceUrl + "/ccp/process", request, CreditCardProcessRequest.class);
            log.info("Payment processed successfully for card number: {}", cardNumber);
        } catch (ResourceAccessException e) {
            log.error("Error accessing the credit card processor service. Card number: {}", cardNumber, e);
            throw new CreditCardProcessorUnavailableException(e);
        } catch (Exception e) {
            log.error("Unexpected error while processing payment for card number: {}", cardNumber, e);
            throw e;
        }
    }
}
