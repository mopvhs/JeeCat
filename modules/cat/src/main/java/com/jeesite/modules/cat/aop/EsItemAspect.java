package com.jeesite.modules.cat.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EsItemAspect {

    // 查询方式
    String queryType() default "itemQuery";

    // 如果为空，默认使用字段名，否者使用指定名称
    String field() default "";

    // get gt
    String rangeOp() default "";

}
