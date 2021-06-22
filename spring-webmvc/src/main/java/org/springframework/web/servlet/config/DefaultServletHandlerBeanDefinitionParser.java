/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.servlet.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * {@link BeanDefinitionParser} that parses a {@code default-servlet-handler} element to
 * register a {@link DefaultServletHttpRequestHandler}.  Will also register a
 * {@link SimpleUrlHandlerMapping} for mapping resource requests, and a
 * {@link HttpRequestHandlerAdapter}.
 *
 * @author Jeremy Grelle
 * @author Rossen Stoyanchev
 * @since 3.0.4
 */
class DefaultServletHandlerBeanDefinitionParser implements BeanDefinitionParser {

	/*
	静态资源处理 <mvc:default-servlet-handler/>
	负责向容器注册一下三个组件：
	1.DefaultServletHttpRequestHandler
	2.SimpleUrlHandlerMapping
	3.HttpRequestHandlerAdapter
	 */

	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);

		String defaultServletName = element.getAttribute("default-servlet-name");
		//注册DefaultServletHttpRequestHandler组件
		RootBeanDefinition defaultServletHandlerDef = new RootBeanDefinition(DefaultServletHttpRequestHandler.class);
		defaultServletHandlerDef.setSource(source);
		defaultServletHandlerDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		if (StringUtils.hasText(defaultServletName)) {
			defaultServletHandlerDef.getPropertyValues().add("defaultServletName", defaultServletName);
		}
		String defaultServletHandlerName = parserContext.getReaderContext().generateBeanName(defaultServletHandlerDef);
		parserContext.getRegistry().registerBeanDefinition(defaultServletHandlerName, defaultServletHandlerDef);
		parserContext.registerComponent(new BeanComponentDefinition(defaultServletHandlerDef, defaultServletHandlerName));

		//注册SimpleUrlHandlerMapping组件
		Map<String, String> urlMap = new ManagedMap<>();
		urlMap.put("/**", defaultServletHandlerName);

		RootBeanDefinition handlerMappingDef = new RootBeanDefinition(SimpleUrlHandlerMapping.class);
		handlerMappingDef.setSource(source);
		handlerMappingDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		handlerMappingDef.getPropertyValues().add("urlMap", urlMap);

		String handlerMappingBeanName = parserContext.getReaderContext().generateBeanName(handlerMappingDef);
		parserContext.getRegistry().registerBeanDefinition(handlerMappingBeanName, handlerMappingDef);
		parserContext.registerComponent(new BeanComponentDefinition(handlerMappingDef, handlerMappingBeanName));

		// Ensure BeanNameUrlHandlerMapping (SPR-8289) and default HandlerAdapters are not "turned off"
		/**
		 * 1.注册BeanNameUrlHandlerMapping
		 * 2.注册HttpRequestHandlerAdapter
		 * 3.注册SimpleControllerHandlerAdapter
		 * 4.注册HandlerMappingIntrospector
		 * 5.注册HandlerMappingIntrospector
		 * 6.注册FixedThemeResolver
		 * 7.注册AcceptHeaderLocaleResolver
		 * 8.注册SessionFlashMapManager
		 * 9.注册DefaultRequestToViewNameTranslator
		 */
		MvcNamespaceUtils.registerDefaultComponents(parserContext, source);

		return null;
	}

}
