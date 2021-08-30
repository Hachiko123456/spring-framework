package com.yhx.learn.annotationtypemapping;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yanghuaxu
 * @date 2021/8/28 11:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface A_C {
	@AliasFor(value = "c2")
	String c1() default "A_C_1";

	@AliasFor(value = "c1")
	String c2() default "A_C_1";

	String c3() default "A_C_3";

	@AliasFor(value = "c5")
	String c4() default "A_C_4";

	@AliasFor(value = "c4")
	String c5() default "A_C_4";

	String c6() default "A_C_6";
}
