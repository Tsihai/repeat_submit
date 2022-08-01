package com.sihai.repeat_submit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重复提交注解
 */
// retained at runtime
@Retention(RetentionPolicy.RUNTIME)
// can be used on classes, methods, fields, parameters, etc.
@Target(ElementType.METHOD)
public @interface RepeatSubmit {

    /**
     * 两个请求之间的最小时间间隔，单位为毫秒
     * @return
     */
    int interval() default 5000;

    /**
     * 重复提交时提示文本
     * @return
     */
    String message() default "请勿重复提交, 请稍后再试";
}
