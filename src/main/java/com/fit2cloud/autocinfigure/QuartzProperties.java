package com.fit2cloud.autocinfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quartz")
public class QuartzProperties {
    private String prefix;
    private String suffix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
