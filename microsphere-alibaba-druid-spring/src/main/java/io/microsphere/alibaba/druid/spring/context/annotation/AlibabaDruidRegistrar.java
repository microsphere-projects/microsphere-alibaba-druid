/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.alibaba.druid.spring.context.annotation;

import com.alibaba.druid.filter.AutoLoad;
import com.alibaba.druid.filter.Filter;
import io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor;
import io.microsphere.spring.beans.BeanSource;
import io.microsphere.spring.context.annotation.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

import static io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor.BEAN_NAME;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSpringFactoriesBeans;
import static io.microsphere.util.ServiceLoaderUtils.getServiceClasses;

/**
 * {@link AnnotatedBeanCapableImportBeanDefinitionRegistrar} class for Alibaba Druid
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // AlibabaDruidRegistrar is automatically activated via @EnableAlibabaDruid
 *   @Configuration
 *   @EnableAlibabaDruid(filterClasses = AbstractStatementFilter.class)
 *   public class AppConfig { }
 *   // AlibabaDruidRegistrar registers DruidDataSourceBeanPostProcessor with the given filter classes
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableAlibabaDruid
 * @see AnnotatedBeanCapableImportBeanDefinitionRegistrar
 * @see ImportBeanDefinitionRegistrar
 * @see DruidDataSourceBeanPostProcessor
 * @since 1.0.0
 */
class AlibabaDruidRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableAlibabaDruid> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableAlibabaDruid> annotationAttributes) {
        Class<? extends Filter>[] filterClasses = (Class<? extends Filter>[]) annotationAttributes.getClassArray("filterClasses");
        BeanSource[] sources = (BeanSource[]) annotationAttributes.get("sources");
        registerFilterBeans(registry, filterClasses, sources);
    }

    private void registerFilterBeans(BeanDefinitionRegistry registry, Class<? extends Filter>[] filterClasses, BeanSource[] sources) {
        for (BeanSource source : sources) {
            switch (source) {
                case SPRING_FACTORIES:
                    registerFiltersBySpringFactories(registry, filterClasses);
                case JAVA_SERVICE_PROVIDER:
                    registerFiltersByJavaServiceProvider(registry, filterClasses);
                case BEAN_FACTORY:
                default:
                    registerDruidDataSourceBeanPostProcessor(registry, filterClasses);
            }
        }
    }

    private void registerFiltersBySpringFactories(BeanDefinitionRegistry registry, Class<? extends Filter>[] filterClasses) {
        registerSpringFactoriesBeans(registry, filterClasses);
    }

    private void registerFiltersByJavaServiceProvider(BeanDefinitionRegistry registry, Class<? extends Filter>[] filterClasses) {
        for (Class<? extends Filter> filterClass : filterClasses) {
            Set<? extends Class<? extends Filter>> serviceClasses = getServiceClasses(filterClass, classLoader);
            for (Class<? extends Filter> serviceClass : serviceClasses) {
                if (serviceClass.isAnnotationPresent(AutoLoad.class)) {
                    // Any filter class annotated @AutoLoad will be added into DruidDataSource automatically,
                    // so it will be ignored here.
                    continue;
                }
                registerBeanDefinition(registry, serviceClass);
            }
        }
    }

    private void registerDruidDataSourceBeanPostProcessor(BeanDefinitionRegistry registry, Class<? extends Filter>[] filterClasses) {
        String beanName = BEAN_NAME;
        Class<?> beanClass = DruidDataSourceBeanPostProcessor.class;
        Object argument = filterClasses;
        registerBeanDefinition(registry, beanName, beanClass, argument);
    }
}