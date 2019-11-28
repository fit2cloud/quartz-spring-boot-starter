package com.fit2cloud.quartz.anno;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * Qualifier annotation for a DataSource to be injected into Quartz auto-configuration.
 * Can be used on a secondary data source, if there is another one marked as
 * {@code @Primary}.
 * Same as org.springframework.boot.autoconfigure.quartz.QuartzDataSource
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE,
        ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface QuartzDataSource {

}
