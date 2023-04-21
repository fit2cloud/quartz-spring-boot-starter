
package com.fit2cloud.quartz.anno;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * Qualifier annotation for a TransactionManager to be injected into Quartz
 * auto-configuration. Can be used on a secondary transaction manager, if there is another
 * one marked as {@code @Primary}.
 * <p>
 * Same as org.springframework.boot.autoconfigure.quartz.QuartzTransactionManager
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface QuartzTransactionManager {

}
