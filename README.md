# Quartz Spring Boot Starter

- [前言](#前言)
- [适用场景](#适用场景)
- [日常使用](#日常使用)
- [注意事项](#注意事项)

## 前言

涵盖范围：

- 本工程对刚接触FIT2CLOUD的同学，以及具有多年经验的老司机都有用处。本工程致力于做到*覆盖面广*（尽量包括一切重要的内容），*具体*（给出最常见的具体的例子），以及*简洁*（避免不必要的，或是可以在其他地方轻松查到的细枝末节）。每个技巧在特定情境下或是基本的，或是能显著节约时间。
- 本文主要介绍工程的入门使用和注意事项。
- 本文为 OS X 所写，并适用于 Windows 和 Linux 。

## 适用场景
- 本文基于`quartz`定时任务进行处理，旨在简化定时任务配置时的繁琐操作。

## 日常使用
- 工程必须是spring boot 工程否则无法使用。
- 集群定时任务启动需要web容器和数据库的支持，各个数据库的`DDL`请自行下载[http://www.quartz-scheduler.org/downloads/](http://www.quartz-scheduler.org/downloads/) 使用时请注意表名大小写的问题。
- 将下面的代码复制到`POM`文件中指定的位置。
```xml
<dependency>
  <groupId>com.fit2cloud</groupId>
  <artifactId>quartz-spring-boot-starter</artifactId>
  <version>0.0.4</version>
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
    /**
    * 动态调整的定时任务
    * @param a 自定义参数 基本类型必须是包装类型
    * @param b 自定义参数 必须实现 java.io.Serializable 接口
    */
    public void task1(Integer a, String b) {
        System.out.printf("%d, %s\n", a, b);
    }
}
```
- 测试动态调整的定时任务
```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestQuartzManageService {
    @Resource
    private QuartzManageService quartzManageService;

    @Test
    public void testAddJob() throws Exception {
        quartzManageService.addJob("commonJob", "task1", "1/5 * * * * ?", 3, "test");
        Thread.sleep(1000 * 1000);
    } 
    
    @Test
    public void testDeleteJob() throws Exception {
        JobKey jobKey = quartzManageService.getJobKey(TriggerKey.triggerKey("commonJob" + "." + "task1"));
        quartzManageService.deleteJob(jobKey);
    }
}

``` 
- 指定数据库连接池

```java
@Bean
@QuartzDataSource // 指定 quartz 的数据库连接池
public DataSource optionalDataSource() throws Exception {
    ComboPooledDataSource dataSource = new ComboPooledDataSource();
    dataSource.setUser(env.getProperty("optional.rdb.user"));
    dataSource.setDriverClass(env.getProperty("optional.rdb.driver"));
    dataSource.setPassword(env.getProperty("optional.rdb.password"));
    dataSource.setJdbcUrl(env.getProperty("optional.rdb.url"));
    // todo 自行添加其他参数
    return dataSource;
}

```

## 注意事项
- 本工程 `0.0.1` 版本不再维护
- 本工程 `0.0.2` 版本基于JDK7编写，支持动态调整定时任务并指定参数执行修改了注解的名称和包路径
- 本工程 `0.0.3` 版本基于JDK8编写，使用时请对照版本操作
- 本工程 `0.0.4` 区分逻辑代码和 autoconfigure 配置
- 本工程 `0.0.5` 可以配置 Thread pool count
- 本工程 `0.0.6` 可以配置数据库连接池
