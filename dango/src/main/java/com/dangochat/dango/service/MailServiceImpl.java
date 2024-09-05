package com.dangochat.dango.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MailServiceImpl implements MailService {
    // MailConfig에서 등록해둔 Bean을 autowired하여 사용하기
    private final JavaMailSender emailSender;

    // 사용자가 메일로 받을 인증번호
    private String key;
    @Value("${naver.id}")
    private String id;
    // Instant 클래스는 특정 지점의 시간을 나타내기 위한 클래스입니다.
    // 코드 생성 시간을 나타내는 Instant 객체입니다.
    private Instant codeGenerationTime;
    // Duration 클래스는 두 시간 간의 차이를 나타내기 위한 클래스입니다.
    // Duration.ofMinutes(1)을 사용하여 1분으로 설정합니다.
    private Duration validityDuration = Duration.ofMinutes(1);


    // 메일 발송
    // sendSimpleMessage 의 매개변수 to는 이메일 주소가 되고,
    // MimeMessage 객체 안에 내가 전송할 메일의 내용을 담는다
    // bean으로 등록해둔 javaMail 객체를 사용하여 이메일을 발송한다
    @Override
    public String sendSimpleMessage(String to) throws Exception {
        key = createKey(); // 랜덤 인증코드 생성
        log.info("********생성된 랜덤 인증코드******** => " + key);

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
        // 메일로 사용자에게 보낸 인증코드를 서버로 반환! 인증코드 일치여부를 확인하기 위함
        return key;
    }

    // 메일 내용 작성
    private MimeMessage creatMessage(String to) throws MessagingException, UnsupportedEncodingException {
        log.info("메일받을 사용자 : " + to);
        log.info("인증번호 : " + key);
        codeGenerationTime = Instant.now();
        log.info("********코드 생성 시간******** => " + codeGenerationTime);
        log.info("********유효 시간******** => " + validityDuration);

        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO, to);
        // 이메일 제목
        message.setSubject("관리자 회원가입을 위한 이메일 인증코드");

        String msgg = "";
        msgg += "<h1>안녕하세요</h1>";
        msgg += "<h1>저희는 BlueBucket 이커머스 플랫폼 입니다</h1>";
        msgg += "<br>";
        msgg += "<br>";
        msgg += "<div align='center' style='border:1px solid black'>";
        msgg += "<h3 style='color:blue'>회원가입 인증코드 입니다</h3>";
        msgg += "<div style='font-size:130%'>";
        msgg += "<strong>" + key + "</strong></div><br/>"; // 메일에 인증번호 ePw 넣기
        msgg += "<p>유효 시간: " + validityDuration.toMinutes() + "분 동안만 유효합니다.</p>";
        msgg += "</div>";
        // 메일 내용, charset타입, subtype
        message.setText(msgg, "utf-8", "html");
        // 보내는 사람의 이메일 주소, 보내는 사람 이름
        message.setFrom(id);
        log.info("********creatMessage 함수에서 생성된 msgg 메시지********" + msgg);
        log.info("********creatMessage 함수에서 생성된 리턴 메시지********" + message);

        return message;
    }

    // 랜덤 인증코드 생성
    private String createKey() throws Exception {
        int length = 6;
        try {
            // SecureRandom.getInstanceStrong()을 호출하여 강력한 (strong) 알고리즘을 사용하는 SecureRandom 인스턴스를 가져옵니다.
            // 이는 예측하기 어려운 안전한 랜덤 값을 제공합니다.
            Random random = SecureRandom.getInstanceStrong();
            // StringBuilder는 가변적인 문자열을 효율적으로 다루기 위한 클래스입니다.
            // 여기서는 생성된 랜덤 값을 문자열로 변환하여 저장하기 위해 StringBuilder를 사용합니다.
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("MemberService.createCode() exception occur");
            throw new Exception(e.getMessage());
        }
    }

    // 사용자가 입력한 인증번호와 서버에서 생성한 인증번호를 비교하는 메서드
    @Override
    public String verifyCode(String code) {
        try {
            if (codeGenerationTime == null) {
                // 시간 정보가 없으면 유효하지 않음
                return "시간 정보가 없습니다.";
            }
            // 현재 시간과 코드 생성 시간의 차이 계산
            Duration elapsedDuration = Duration.between(codeGenerationTime, Instant.now());
            // 남은 시간 계산: 전체 유효 기간에서 경과된 시간을 뺀다
            long remainDuration = validityDuration.minus(elapsedDuration).getSeconds();

            // 시간이 0보다 높으면 즉, 유효기간이 지나지 않으면
            // 사용자가 입력한 인증번호와 서버에서 생성한 인증번호를 비교해서 맞다면 true
            if (remainDuration > 0) {
                if (code.equals(key)) {
                    return "인증 번호가 일치합니다.";
                }
            } else if (remainDuration < 0) {
                return "인증 번호의 유효시간이 지났습니다.";
            } else if (!code.equals(key)) {
                return "인증 번호가 일치하지 않습니다.";
            }
            return null;
        } catch (NullPointerException e) {
            // 사용자가 정수가 아닌 값을 입력한 경우
            return "유효하지 않는 인증 번호를 입력했습니다.";
        }
    }

}
