package com.fit2cloud.quartz.util;

import org.quartz.JobDetail;
import org.quartz.Trigger;

public class JobDetailTrigger {
    JobDetail jobDetail;
    Trigger trigger;

    public JobDetailTrigger(JobDetail jobDetail, Trigger trigger) {
        this.jobDetail = jobDetail;
        this.trigger = trigger;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public Trigger getTrigger() {
        return trigger;
    }
}