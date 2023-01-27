package com.maskting.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maskting.backend.domain.VerificationNumber;
import com.maskting.backend.dto.request.CheckSmsRequest;
import com.maskting.backend.dto.request.MessageRequest;
import com.maskting.backend.dto.request.SmsRequest;
import com.maskting.backend.dto.response.SmsResponse;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.repository.VerificationNumberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SmsService {
    private static final int START_BOUNDARY = 100000;
    private static final int END_BOUNDARY = 1000000;
    private static final String SMS_URI = "https://sens.apigw.ntruss.com/sms/v2/services/";

    @Value("${naver.cloud.sms.accessKey}")
    private String accessKey;

    @Value("${naver.cloud.sms.secretKey}")
    private String secretKey;

    @Value("${naver.cloud.sms.serviceId}")
    private String serviceId;

    @Value("${naver.cloud.sms.senderNumber}")
    private String senderNumber;

    private final UserRepository userRepository;
    private final VerificationNumberRepository verificationNumberRepository;

    @Transactional
    public SmsResponse sendSms(String receiverNumber) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException, URISyntaxException {
        deleteVerificationNumber(receiverNumber);
        int randomNumber = generateVerificationNumber();
        Long time = System.currentTimeMillis();

        saveVerificationNumber(receiverNumber, randomNumber);
        HttpEntity<String> httpBody = getHttpBody(getHttpHeaders(time), getSmsRequest(randomNumber, receiverNumber));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return restTemplate.postForObject(new URI(SMS_URI + serviceId +"/messages"), httpBody, SmsResponse.class);
    }

    private void deleteVerificationNumber(String receiverNumber) {
        Optional<VerificationNumber> temp = verificationNumberRepository.findById(receiverNumber);
        temp.ifPresent(verificationNumberRepository::delete);
    }

    private static HttpEntity<String> getHttpBody(HttpHeaders headers, SmsRequest smsRequest) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(smsRequest);
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);
        return httpBody;
    }

    private void saveVerificationNumber(String receiverNumber, int randomNumber) {
        VerificationNumber verificationNumber = new VerificationNumber(receiverNumber, String.valueOf(randomNumber));
        verificationNumberRepository.save(verificationNumber);
    }

    private SmsRequest getSmsRequest(int randomNumber, String receiverNumber) {
        List<MessageRequest> messages = new ArrayList<>();
        messages.add(new MessageRequest(receiverNumber));

        return new SmsRequest("SMS",
                this.senderNumber,
                "인증번호: " + randomNumber,
                messages);
    }

    private HttpHeaders getHttpHeaders(Long time) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time.toString());
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", makeSignature(time.toString()));
        return headers;
    }

    private String makeSignature(String timeStamp) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + serviceId + "/messages";

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timeStamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }

    private static int generateVerificationNumber() {
        return ThreadLocalRandom.current().nextInt(START_BOUNDARY, END_BOUNDARY);
    }

    public boolean checkVerificationNumber(CheckSmsRequest checkSmsRequest) {
        VerificationNumber verificationNumber = verificationNumberRepository.findById(checkSmsRequest.getPhoneNumber()).orElseThrow();
        return checkSmsRequest.getVerificationNumber().equals(verificationNumber.getValue());
    }
}
