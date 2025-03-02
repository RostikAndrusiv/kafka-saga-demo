package com.rostik.andrusiv.payment.service;

import com.rostik.andrusiv.core.dto.CreditCardProcessRequest;
import com.rostik.andrusiv.core.exception.CreditCardProcessorUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;

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
        try {
            var request = new CreditCardProcessRequest(cardNumber, paymentAmount);
            restTemplate.postForObject("http://" + ccpRemoteServiceUrl + "/ccp/process", request, CreditCardProcessRequest.class);
        } catch (ResourceAccessException e) {
            throw new CreditCardProcessorUnavailableException(e);
        }
    }
}
