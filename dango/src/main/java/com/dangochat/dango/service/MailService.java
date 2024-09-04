package com.dangochat.dango.service;

public interface MailService {
    // 메일 발송
    String sendSimpleMessage(String to)throws Exception;

    // 검증
    String verifyCode(String code);
}
