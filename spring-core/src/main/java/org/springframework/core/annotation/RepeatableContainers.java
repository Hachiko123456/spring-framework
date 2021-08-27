/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.annotation;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 可重复注解容器
 * {@link Repeatable}注解容器类
 * {@link Repeatable}作用：如果想在一个类上标注重复的注解（例如@A），则需要指明@A注解的容器，
 * 指明容器则用{@link Repeatable}
 * Strategy used to determine annotations that act as containers for other
 * annotations. The {@link #standardRepeatables()} method provides a default
 * strategy that respects Java's {@link Repeatable @Repeatable} support and
 * should be suitable for most situations.
 *
 * <p>The {@link #of} method can be used to register relationships for
 * annotations that do not wish to use {@link Repeatable @Repeatable}.
 *
 * <p>To completely disable repeatable support use {@link #none()}.
 *
 * @author Phillip Webb
 * @since 5.2
 */
public abstract class RepeatableContainers {

	@Nullable
	private final RepeatableContainers parent;


	private RepeatableContainers(@Nullable RepeatableContainers parent) {
		this.parent = parent;
	}


	/**
	 * Add an additional explicit relationship between a contained and
	 * repeatable annotation.
	 * @param container the container type
	 * @param repeatable the contained repeatable type
	 * @return a new {@link RepeatableContainers} instance
	 */
	public RepeatableContainers and(Class<? extends Annotation> container,
			Class<? extends Annotation> repeatable) {

		return new ExplicitRepeatableContainer(this, repeatable, container);
	}

	@Nullable
	Annotation[] findRepeatedAnnotations(Annotation annotation) {
		if (this.parent == null) {
			return null;
		}
		return this.parent.findRepeatedAnnotations(annotation);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (other == this) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		return ObjectUtils.nullSafeEquals(this.parent, ((RepeatableContainers) other).parent);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.parent);
	}


	/**
	 * Create a {@link RepeatableContainers} instance that searches using Java's
	 * {@link Repeatable @Repeatable} annotation.
	 * @return a {@link RepeatableContainers} instance
	 */
	public static RepeatableContainers standardRepeatables() {
		return StandardRepeatableContainers.INSTANCE;
	}

	/**
	 * Create a {@link RepeatableContainers} instance that uses a defined
	 * container and repeatable type.
	 * @param repeatable the contained repeatable annotation
	 * @param container the container annotation or {@code null}. If specified,
	 * this annotation must declare a {@code value} attribute returning an array
	 * of repeatable annotations. If not specified, the container will be
	 * deduced by inspecting the {@code @Repeatable} annotation on
	 * {@code repeatable}.
	 * @return a {@link RepeatableContainers} instance
	 */
	public static RepeatableContainers of(
			Class<? extends Annotation> repeatable, @Nullable Class<? extends Annotation> container) {

		return new ExplicitRepeatableContainer(null, repeatable, container);
	}

	/**
	 * Create a {@link RepeatableContainers} instance that does not expand any
	 * repeatable annotations.
	 * @return a {@link RepeatableContainers} instance
	 */
	public static RepeatableContainers none() {
		return NoRepeatableContainers.INSTANCE;
	}


	/**
	 * 标准实现，搜索被{@link Repeatable @Repeatable}注解的注解
	 * Standard {@link RepeatableContainers} implementation that searches using
	 * Java's {@link Repeatable @Repeatable} annotation.
	 */
	private static class StandardRepeatableContainers extends RepeatableContainers {

		private static final Map<Class<? extends Annotation>, Object> cache = new ConcurrentReferenceHashMap<>();

		private static final Object NONE = new Object();

		private static StandardRepeatableContainers INSTANCE = new StandardRepeatableContainers();

		StandardRepeatableContainers() {
			super(null);
		}

		/**
		 * 查找可重复的注解数组
		 * @param annotation
		 * @return
		 */
		@Override
		@Nullable
		Annotation[] findRepeatedAnnotations(Annotation annotation) {
			Method method = getRepeatedAnnotationsMethod(annotation.annotationType());
			if (method != null) {
				return (Annotation[]) ReflectionUtils.invokeMethod(method, annotation);
			}
			return super.findRepeatedAnnotations(annotation);
		}

		@Nullable
		private static Method getRepeatedAnnotationsMethod(Class<? extends Annotation> annotationType) {
			Object result = cache.computeIfAbsent(annotationType,
					StandardRepeatableContainers::computeRepeatedAnnotationsMethod);
			return (result != NONE ? (Method) result : null);
		}

		/**
		 * 获取返回值类型被{@link Repeatable @Repeatable}注解，且只有一个value方法的注解
		 * @param annotationType
		 * @return
		 */
		private static Object computeRepeatedAnnotationsMethod(Class<? extends Annotation> annotationType) {
			// 把注解对应的Class类型里面的方法转为AttributeMethods
			AttributeMethods methods = AttributeMethods.forAnnotationType(annotationType);
			// 如果该注解只包含一个value方法
			if (methods.hasOnlyValueAttribute()) {
				Method method = methods.get(0);
				// 获取方法的返回值类型
				Class<?> returnType = method.getReturnType();
				// 如果返回值是个数组
				if (returnType.isArray()) {
					Class<?> componentType = returnType.getComponentType();
					// 如果数组的`类型`是一个注解且该注解被@Repeatable标注，则返回该方法
					if (Annotation.class.isAssignableFrom(componentType) &&
							componentType.isAnnotationPresent(Repeatable.class)) {
						return method;
					}
				}
			}
			//否则返回NONE
			return NONE;
		}
	}


	/**
	 * 明确重复类型的Repeatable容器
	 * A single explicit mapping.
	 */
	private static class ExplicitRepeatableContainer extends RepeatableContainers {

		/**
		 * 自定义的重复注解
		 */
		private final Class<? extends Annotation> repeatable;

		/**
		 * 自定义的容器注解类型（如果没有自定义，则用默认的{@link Repeatable}）
		 */
		private final Class<? extends Annotation> container;

		/**
		 * value方法
		 */
		private final Method valueMethod;

		ExplicitRepeatableContainer(@Nullable RepeatableContainers parent,
				Class<? extends Annotation> repeatable, @Nullable Class<? extends Annotation> container) {

			super(parent);
			Assert.notNull(repeatable, "Repeatable must not be null");
			/**
			 * 如果自定义的容器为空，则用{@link Repeatable}的value值
			 */
			if (container == null) {
				container = deduceContainer(repeatable);
			}
			// 获取容器类的value方法
			Method valueMethod = AttributeMethods.forAnnotationType(container).get(MergedAnnotation.VALUE);
			try {
				// value方法为空则抛出异常
				if (valueMethod == null) {
					throw new NoSuchMethodException("No value method found");
				}
				// 获取value方法的返回值类型
				Class<?> returnType = valueMethod.getReturnType();
				// 如果返回值类型不是数组，或者数组的元素类型不是可重复的类型，则抛出异常
				if (!returnType.isArray() || returnType.getComponentType() != repeatable) {
					throw new AnnotationConfigurationException("Container type [" +
							container.getName() +
							"] must declare a 'value' attribute for an array of type [" +
							repeatable.getName() + "]");
				}
			}
			catch (AnnotationConfigurationException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				throw new AnnotationConfigurationException(
						"Invalid declaration of container type [" + container.getName() +
								"] for repeatable annotation [" + repeatable.getName() + "]",
						ex);
			}
			this.repeatable = repeatable;
			this.container = container;
			this.valueMethod = valueMethod;
		}

		/**
		 * 推断出容器类，其实就是拿{@link Repeatable}的value值
		 * @param repeatable
		 * @return
		 */
		private Class<? extends Annotation> deduceContainer(Class<? extends Annotation> repeatable) {
			Repeatable annotation = repeatable.getAnnotation(Repeatable.class);
			Assert.notNull(annotation, () -> "Annotation type must be a repeatable annotation: " +
						"failed to resolve container type for " + repeatable.getName());
			return annotation.value();
		}

		@Override
		@Nullable
		Annotation[] findRepeatedAnnotations(Annotation annotation) {
			if (this.container.isAssignableFrom(annotation.annotationType())) {
				return (Annotation[]) ReflectionUtils.invokeMethod(this.valueMethod, annotation);
			}
			return super.findRepeatedAnnotations(annotation);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (!super.equals(other)) {
				return false;
			}
			ExplicitRepeatableContainer otherErc = (ExplicitRepeatableContainer) other;
			return (this.container.equals(otherErc.container) && this.repeatable.equals(otherErc.repeatable));
		}

		@Override
		public int hashCode() {
			int hashCode = super.hashCode();
			hashCode = 31 * hashCode + this.container.hashCode();
			hashCode = 31 * hashCode + this.repeatable.hashCode();
			return hashCode;
		}
	}


	/**
	 * No repeatable containers.
	 */
	private static class NoRepeatableContainers extends RepeatableContainers {

		private static NoRepeatableContainers INSTANCE = new NoRepeatableContainers();

		NoRepeatableContainers() {
			super(null);
		}
	}

}
