package com.fit2cloud.autocinfigure;

public class QuartzConfigService {

    private String prefix;
    private String suffix;

    public QuartzConfigService(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }
    public String wrap(String word) {
        return prefix + word + suffix;
    }
}
