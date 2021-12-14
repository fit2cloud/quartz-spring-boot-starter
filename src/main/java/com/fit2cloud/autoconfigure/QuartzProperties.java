package com.fit2cloud.autoconfigure;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;


@ConfigurationProperties(prefix = "quartz", ignoreUnknownFields = true)
public class QuartzProperties implements EnvironmentAware, InitializingBean {

    private boolean enabled = false;
    private String schedulerName;
    private String timeZone = "Asia/Shanghai";
    private Integer threadCount = 10;
    private final Map<String, String> properties = new HashMap<>();

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public void setEnvironment(Environment environment) {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
