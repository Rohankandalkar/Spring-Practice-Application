package com.hcl.ott.ingestion.configuration;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThradPoolExecuterConfig
{
    @Bean(name = "OttIngetionExecutor")     
    public Executor asyncApplicationExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(7);
        executor.setMaxPoolSize(42);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("Ott-Ingetion-Executor-");
        executor.initialize();
        return executor;
    }


    @Bean(name = "OttIngetionMultipartExecutor")
    public Executor asyncMultipartExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(42);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Ott-Ingetion-Multipart-Executor-");
        executor.initialize();
        return executor;
    }
}
