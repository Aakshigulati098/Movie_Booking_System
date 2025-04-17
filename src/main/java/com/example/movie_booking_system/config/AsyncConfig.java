package com.example.movie_booking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);         // Initial threads
        executor.setMaxPoolSize(10);         // Max concurrent threads
        executor.setQueueCapacity(100);      // Queue before rejecting
        executor.setThreadNamePrefix("AsyncExecutor-");

        executor.initialize();
        return executor;
    }
}
