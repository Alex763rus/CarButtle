package org.example.my.config;

import org.example.my.ai.dynamic.CustomAIManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfig {

    @Bean
    public CustomAIManager customAIManager() {
        return new CustomAIManager();
    }
}