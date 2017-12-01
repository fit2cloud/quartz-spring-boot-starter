package com.fit2cloud.autocinfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@ConditionalOnClass(QuartzConfigService.class)
@EnableConfigurationProperties(QuartzProperties.class)
public class QuartzAutoConfigure {

    @Resource
    private QuartzProperties properties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "quartz", value = "enabled", havingValue = "true")
    QuartzConfigService exampleService() {
        return new QuartzConfigService(properties.getPrefix(), properties.getSuffix());
    }

}
