package com.yhx.learn.annotationtypemapping;

import org.junit.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;

/**
 * @author yanghuaxu
 * @date 2021/8/28 11:37
 */
@A_ROOT
public class AnnotationUtilTest {

	@Test
	public void testAnnotation() {
		AnnotationAttributes a = AnnotatedElementUtils.findMergedAnnotationAttributes(AnnotationUtilTest.class, A_A.class, false, true);
		AnnotationAttributes b = AnnotatedElementUtils.findMergedAnnotationAttributes(AnnotationUtilTest.class, A_B.class, false, true);
		AnnotationAttributes c = AnnotatedElementUtils.findMergedAnnotationAttributes(AnnotationUtilTest.class, A_C.class, false, true);
		AnnotationAttributes root = AnnotatedElementUtils.findMergedAnnotationAttributes(AnnotationUtilTest.class, A_ROOT.class, false, true);
		System.out.println("x");
	}

	@Test
	public void testAnnotationUse() {
		AnnotationAttributes a = AnnotatedElementUtils.findMergedAnnotationAttributes(AnnotationUtilTest.class, A_A.class, false, true);
		AnnotationAttributes b = AnnotatedElementUtils.findMergedAnnotationAttributes(AnnotationUtilTest.class, A_B.class, false, true);
		AnnotationAttributes c = AnnotatedElementUtils.findMergedAnnotationAttributes(AnnotationUtilTest.class, A_C.class, false, true);
		AnnotationAttributes root = AnnotatedElementUtils.findMergedAnnotationAttributes(AnnotationUtilTest.class, A_ROOT.class, false, true);
		System.out.println("x");
	}

}
