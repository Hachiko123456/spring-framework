package com.yhx.learn.annotationtypemapping;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yanghuaxu
 * @date 2021/8/28 11:33
 */
@A_B
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface A_A {

	@AliasFor(value = "c1", annotation = A_C.class)
	String a1() default "A_A_1";

	String a2() default "A_A_2";

}
