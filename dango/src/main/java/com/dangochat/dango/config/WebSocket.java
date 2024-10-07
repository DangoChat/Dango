package com.dangochat.dango.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocket implements WebSocketMessageBrokerConfigurer{

    // @Override
    // public void registerStompEndpoints(StompEndpointRegistry registry){
    //     // STOMP 엔드 포인트 설정
    //     registry.addEndpoint("/chat") // socket 연결 url
    //             .setAllowedOriginPatterns("*"); // Cors 허용 범위
    // }

    // @Override
    // public void configureMessageBroker(MessageBrokerRegistry registry) {
    //     // 메시지 브로커 설정: 클라이언트가 구독할 경로
    //     registry.enableSimpleBroker("/topic"); 
    //     // 서버로 보내는 메시지의 경로 ( prefix 정의 )
    //     registry.setApplicationDestinationPrefixes("/app");
    // }
    // @Override
    // public void configureMessageBroker(MessageBrokerRegistry config) {
    //     config.enableSimpleBroker("/topic");
    //     config.setApplicationDestinationPrefixes("/app");
    // }

    // @Override
    // public void registerStompEndpoints(StompEndpointRegistry registry) {
    //     // setAllowedOrigins("*") 대신 setAllowedOriginPatterns("*")를 사용하여 모든 도메인 허용
    //     registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    // }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}