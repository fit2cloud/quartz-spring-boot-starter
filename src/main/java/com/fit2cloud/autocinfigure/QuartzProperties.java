package com.fit2cloud.autocinfigure;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;


@ConfigurationProperties(prefix = "quartz", ignoreUnknownFields = true)
public class QuartzProperties implements EnvironmentAware, InitializingBean {


    private String schedulerName;

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    @Override
    public void setEnvironment(Environment environment) {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
