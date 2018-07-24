package com.fit2cloud.quartz.config;


import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * CustomQuartzJobBean代替org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean，解决序列化的问题
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ClusterQuartzFixedDelayJobBean extends ClusterQuartzJobBean {

}
