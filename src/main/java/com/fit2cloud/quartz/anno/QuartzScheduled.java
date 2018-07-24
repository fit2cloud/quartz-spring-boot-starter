package com.fit2cloud.quartz.anno;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QuartzScheduled {

    /**
     * A cron-like expression, extending the usual UN*X definition to include
     * triggers on the second as well as minute, hour, day of month, month
     * and day of week.  e.g. <code>"0 * * * * MON-FRI"</code> means once
     * per minute on weekdays (at the top of the minute - the 0th second).
     *
     * @return an expression that can be parsed to a cron schedule
     */
    String cron() default "";

    /**
     * Execute the annotated method with a fixed period between the end
     * of the last invocation and the start of the next.
     *
     * @return the delay in milliseconds
     */
    long fixedDelay() default -1;

    /**
     * Execute the annotated method with a fixed period in milliseconds between
     * invocations.
     *
     * @return the period in milliseconds
     */
    long fixedRate() default -1;

    /**
     * Number of milliseconds to delay before the first execution of a
     * {@link #fixedRate()} or {@link #fixedDelay()} task.
     *
     * @return the initial delay in milliseconds
     */
    long initialDelay() default 0;
}
