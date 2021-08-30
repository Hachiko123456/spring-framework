package com.yhx.learn.annotationtypemapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yanghuaxu
 * @date 2021/8/28 11:34
 */
@A_A
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface A_ROOT {
}
