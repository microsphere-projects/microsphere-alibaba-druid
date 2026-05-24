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

import com.alibaba.druid.filter.Filter;
import io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor;
import io.microsphere.spring.context.annotation.BeanCapableImportCandidate;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

import static io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor.BEAN_NAME;
import static io.microsphere.collection.ListUtils.forEach;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactories;
import static io.microsphere.util.StringUtils.uncapitalize;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * {@link BeanCapableImportCandidate}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // AlibabaDruidRegistrar is automatically activated via @EnableAlibabaDruid
 *   @Configuration
 *   @EnableAlibabaDruid(filterBeanClasses = AbstractStatementFilter.class)
 *   public class AppConfig { }
 *   // AlibabaDruidRegistrar registers DruidDataSourceBeanPostProcessor with the given filter classes
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableAlibabaDruid
 * @see BeanCapableImportCandidate
 * @see ImportBeanDefinitionRegistrar
 * @see DruidDataSourceBeanPostProcessor
 * @since 1.0.0
 */
class AlibabaDruidRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {

    private static final String ANNOTATION_CLASS_NAME = EnableAlibabaDruid.class.getName();

    /**
     * Register {@link DruidDataSourceBeanPostProcessor} bean definition with filter bean classes
     * extracted from the {@link EnableAlibabaDruid} annotation metadata.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Invoked automatically by Spring when @EnableAlibabaDruid is present on a @Configuration class
     *   @EnableAlibabaDruid(filterBeanClasses = LoggingStatementFilter.class)
     *   @Configuration
     *   public class AppConfig { }
     *   // A DruidDataSourceBeanPostProcessor bean is registered with LoggingStatementFilter class
     * }</pre>
     *
     * @param metadata the annotation metadata of the importing class
     * @param registry the bean definition registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_CLASS_NAME));
        registerDruidDataSourceBeanPostProcessor(registry, attributes);
        registerFiltersBySpringFactories(registry);
    }

    private void registerDruidDataSourceBeanPostProcessor(BeanDefinitionRegistry registry, AnnotationAttributes attributes) {
        Class<? extends Filter>[] filterBeanClasses = (Class<? extends Filter>[]) attributes.getClassArray("filterBeanClasses");
        String beanName = BEAN_NAME;
        Class<?> beanClass = DruidDataSourceBeanPostProcessor.class;
        Object argument = filterBeanClasses;
        registerBeanDefinition(registry, beanName, beanClass, argument);
    }

    private void registerFiltersBySpringFactories(BeanDefinitionRegistry registry) {
        List<Filter> filters = loadFactories(getApplicationContext(), Filter.class);
        forEach(filters, filter -> {
            String beanName = uncapitalize(filter.getClass().getSimpleName());
            registerBean(registry, beanName, filter);
        });
    }
}
