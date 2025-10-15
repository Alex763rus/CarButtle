package org.example.my.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Включаем простой брокер сообщений
        config.enableSimpleBroker("/topic", "/queue");
        // Префикс для сообщений к методам @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // Префикс для пользовательских сообщений
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Регистрируем endpoint для WebSocket
        registry.addEndpoint("/ws-game")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}