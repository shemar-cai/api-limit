package com.cxming.limit;

import java.lang.annotation.*;

/**
 * @author caixiaoming
 * @create 2021-03-21 23:54
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface ApiLimit {

    /**
     * 每秒处理的额定请求数
     */
    long pps() default Long.MAX_VALUE;

    /**
     * 达到阈值时请求，是否阻塞
     */
    boolean block() default false;

    /**
     * 限流时抛出的异常类型
     */
    Class<? extends Exception> handle() default OutOfLimitException.class;
}
