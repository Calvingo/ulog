package com.ulog.backend.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "selfValueTaskExecutor")
    public Executor selfValueTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：2个（足够处理并发请求）
        executor.setCorePoolSize(2);
        
        // 最大线程数：5个（高峰期处理能力）
        executor.setMaxPoolSize(5);
        
        // 队列容量：50个任务
        executor.setQueueCapacity(50);
        
        // 线程名前缀
        executor.setThreadNamePrefix("SelfValue-Async-");
        
        // 拒绝策略：调用者运行（确保任务不丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 线程空闲时间：60秒后回收
        executor.setKeepAliveSeconds(60);
        
        // 等待任务完成后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("SelfValue async executor configured: core={}, max={}, queue={}", 
            executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}
