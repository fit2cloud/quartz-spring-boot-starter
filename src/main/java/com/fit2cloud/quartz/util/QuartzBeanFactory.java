package com.fit2cloud.quartz.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class QuartzBeanFactory implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public static <T> T getBean(Class<T> tClass) {
        return applicationContext.getBean(tClass);
    }

    public static <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class<T> tClass) {
        return applicationContext.getBean(beanName, tClass);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        QuartzBeanFactory.applicationContext = applicationContext;
    }
}
