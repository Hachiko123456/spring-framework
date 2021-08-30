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
@A_C
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface A_B {

	@AliasFor(value = "c2", annotation = A_C.class)
	String b1() default "A_B_1_2";

	@AliasFor(value = "c2", annotation = A_C.class)
	String b2() default "A_B_1_2";

	@AliasFor(value = "c2", annotation = A_C.class)
	String b3() default "A_B_1_2";

	String b4() default "A_B_4";

}
