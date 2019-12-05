package com.small.mq.client.consumer.annotation;

import java.lang.annotation.*;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 3:26 PM
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SmallMqConsumer {

    String DEFAULT_GROUP = "DEFAULT";   // default group

    String EMPTY_GROUP = "";            // empty group means consume broadcase message, will replace by uuid

    /**
     * @return
     */
    String group() default DEFAULT_GROUP;

    /**
     * @return
     */
    String topic();

    /**
     * @return
     */
    boolean transaction() default true;

}
