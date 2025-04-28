package com.blr19c.blr.free.ai.config.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 通用的spring配置
 */
@AutoConfiguration
public class SpringAutoConfiguration {

    /**
     * springBean工具(静态的ApplicationContext)
     */
    @Bean
    public SpringBeanUtils springBeanUtils() {
        return new SpringBeanUtils();
    }
}
