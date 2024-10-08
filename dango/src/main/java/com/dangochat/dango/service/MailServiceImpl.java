package com.dangochat.dango.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.security.AESUtil;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Map;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MailServiceImpl implements MailService {
    private final JavaMailSender emailSender;
    private final Map<String, String> verificationCodes = new HashMap<>(); // 이메일-인증 코드 매핑

    @Value("${naver.id}")
    private String id;

    @Override
    public String sendSimpleMessage(String to) throws Exception {
        String verificationCode = generateVerificationCode();
        verificationCodes.put(to, verificationCode); // 메모리에 인증 코드 저장
        System.out.println("make verify : " + verificationCode);
        MimeMessage message = createMessage(to, verificationCode); 
        log.info("********생성된 메시지******** => " + message);
        try {
            emailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
        return verificationCode;
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(899999) + 100000; // 6자리 코드 생성
        return String.valueOf(code);
    }

    private MimeMessage createMessage(String to, String verificationCode) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO, to);
        message.setSubject("Dango - 이메일 인증코드");

        String msgg = "<h1>안녕하세요</h1>"
                    + "<p>아래 인증 코드를 입력하여 이메일을 인증하세요:</p>"
                    + "<h2>" + verificationCode + "</h2>";
        message.setText(msgg, "utf-8", "html");
        message.setFrom(id);
        return message;
    }
}