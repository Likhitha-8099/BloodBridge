package com.bloodbridge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Dedicated ThreadPoolTaskExecutor configuration for emergency email dispatches.
 * Controls SMTP concurrency, queueing, and thread management with safe uncaught exception handling.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncEmailConfig implements AsyncConfigurer {

    @Bean(name = "emergencyEmailExecutor")
    public Executor emergencyEmailExecutor() {
        log.info("[ASYNC-CONFIG] Initializing dedicated ThreadPoolTaskExecutor 'emergencyEmailExecutor': CorePoolSize=10, MaxPoolSize=50, QueueCapacity=1000");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("emergency-mail-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("[EMAIL-ASYNC-ERROR] Uncaught exception in async method: {} - Type: {}, Message: {}",
                    method.getName(), throwable.getClass().getSimpleName(), throwable.getMessage());
        };
    }
}
