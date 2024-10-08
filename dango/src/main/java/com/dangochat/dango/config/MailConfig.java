package com.dangochat.dango.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
	
	@Value("${naver.id}")
	private String id;
	@Value("${naver.password}")
	private String password;
	
	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost("smtp.naver.com");
		javaMailSender.setUsername(id);
		javaMailSender.setPassword(password);
		javaMailSender.setPort(465);   //엥 포트번호가 바뀐건가?
		
		javaMailSender.setJavaMailProperties(getMailProperties());
		javaMailSender.setDefaultEncoding("UTF-8");
		
		return javaMailSender;
	}
	
	private Properties getMailProperties() {
		Properties properties = new Properties();
		properties.setProperty("mail.transport.protocol", "smtp");   //프로토콜 설정
		properties.setProperty("mail.smtp.auth", "true");           // smtp 인증
	//  properties.setProperty("mail.smtp.starttls.enable", "true");    
		properties.setProperty("mail.smtp.ssl.trust", "smtp.naver.com");
		properties.setProperty("mail.debug", "true");
		properties.setProperty("mail.smtp.ssl.enable", "true");
		return properties;
	}
}
