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

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MailServiceImpl implements MailService {
    // MailConfig에서 등록해둔 Bean을 autowired하여 사용하기
    private final JavaMailSender emailSender;
    private final MemberRepository memberRepository;

    @Value("${naver.id}")
    private String id;


    // 메일 발송
    // sendSimpleMessage 의 매개변수 to는 이메일 주소가 되고,
    // MimeMessage 객체 안에 내가 전송할 메일의 내용을 담는다
    // bean으로 등록해둔 javaMail 객체를 사용하여 이메일을 발송한다
    @Override
    public String sendSimpleMessage(String to) throws Exception {
        MimeMessage message = creatMessage(to); // "to" 로 메일 발송
        log.info("********생성된 메시지******** => " + message);
        // 예외처리
        try {
            // 이게 메일로 보내주는 메소드
            emailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
        return null;
    }

    // 메일 내용 작성
    private MimeMessage creatMessage(String to) throws MessagingException, UnsupportedEncodingException {
        log.info("메일받을 사용자 : " + to);
        MemberEntity entity = memberRepository.findByUserEmail(to);
        String password = entity.getUserPassword();
        String decryptedPassword = null;
    
        try {
            // AESUtil을 사용하여 비밀번호 복호화
            decryptedPassword = AESUtil.decrypt(password);
        } catch (Exception e) {
            log.error("비밀번호 복호화에 실패했습니다.", e);
        }
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO, to);
        // 이메일 제목
        message.setSubject("관리자 회원가입을 위한 이메일 인증코드");

        String msgg = "";
        msgg += "<h1>안녕하세요</h1>";
        msgg += "<h1>저희는 Dango 입니다</h1>";
        msgg += "<br>";
        msgg += "<br>";
        msgg += "<div align='center' style='border:1px solid black'>";
        msgg += "<h3 style='color:black'>" +  to + "님의 비밀번호입니다</h3>";
        msgg += "<div style='font-size:130%'>";
        msgg += "<strong>" + decryptedPassword + "</strong></div><br/>"; // 메일에 인증번호 ePw 넣기
        // msgg += "<p>유효 시간: " + validityDuration.toMinutes() + "분 동안만 유효합니다.</p>";
        msgg += "</div>";
        // 메일 내용, charset타입, subtype
        message.setText(msgg, "utf-8", "html");
        // 보내는 사람의 이메일 주소, 보내는 사람 이름
        message.setFrom(id);
        log.info("********creatMessage 함수에서 생성된 msgg 메시지********" + msgg);
        log.info("********creatMessage 함수에서 생성된 리턴 메시지********" + message);

        return message;
    }

}
