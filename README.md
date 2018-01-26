# Quartz Spring Boot Starter

- [前言](#前言)
- [基础](#基础)
- [日常使用](#日常使用)
- [注意事项](#注意事项)

## 前言

涵盖范围：

- 本工程对刚接触FIT2CLOUD的同学，以及具有多年经验的老司机都有用处。本工程致力于做到*覆盖面广*（尽量包括一切重要的内容），*具体*（给出最常见的具体的例子），以及*简洁*（避免不必要的，或是可以在其他地方轻松查到的细枝末节）。每个技巧在特定情境下或是基本的，或是能显著节约时间。
- 本文主要介绍工程的入门使用和注意事项。
- 本文为 OX X 所写，并适用于 Windows 和 Linux 。

## 基础
- 本文基于`quartz`定时任务进行处理，旨在简化定时任务配置时的繁琐操作。

## 日常使用
- 工程必须是spring boot 工程否则无法使用。
- 将下面的代码复制到`POM`文件中指定的位置
```xml
<dependency>
  <groupId>com.fit2cloud</groupId>
  <artifactId>quartz-spring-boot-starter</artifactId>
  <version>0.0.2</version>
</dependency>
```
- 工程启动的配置文件`application.properties`中有定时任务的详细设置
```properties
# quartz enabled
quartz.enabled=true # 是否开启quartz
quartz.scheduler-name=testScheduler # 集群定时任务的唯一标识
```
- 工程中代码只需要在 `public method` 中加上指定注解即可
```java
@Service
public class CustomDemoJob {
    /**
     * 可以直接写表达式，也可以写配置文件里的key
     * 1/5 * * * * ?
     */
    @QuartzScheduled(cron = "${cron.expression.demo}", initialDelay = 1000 * 120)
    public void cronJob() throws Exception {
        System.out.println(Thread.currentThread() + "cronJob start " + new Date());
        Thread.sleep(10 * 1000);
        System.out.println(Thread.currentThread() + "cronJob end " + new Date());
    }  
    @QuartzScheduled(fixedDelay = 1000 * 5)
    public void fixedDealyJob() throws Exception {
        System.out.println(Thread.currentThread() + "fixedDealyJob start " + new Date());
        Thread.sleep(10 * 1000);
        System.out.println(Thread.currentThread() + "fixedDealyJob end " + new Date());  
    }
  
    @QuartzScheduled(fixedRate = 1000 * 5, initialDelay = 1000 * 120)
    public void fixedRateJob() throws Exception {
        System.out.println(Thread.currentThread() + "fixedRateJob start " + new Date());
        Thread.sleep(10 * 1000);
        System.out.println(Thread.currentThread() + "fixedRateJob end " + new Date());   
    }
}
``` 

## 注意事项
- 本工程版本 `0.0.2` 版本修改了注解的名称和包路径，使用时请对照版本操作
