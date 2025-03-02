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

import static io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor.BEAN_NAME;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * {@link BeanCapableImportCandidate}
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

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_CLASS_NAME));
        registerDruidDataSourceBeanPostProcessor(registry, attributes);
    }

    private void registerDruidDataSourceBeanPostProcessor(BeanDefinitionRegistry registry, AnnotationAttributes attributes) {
        Class<? extends Filter>[] filterBeanClasses = (Class<? extends Filter>[]) attributes.getClassArray("filterBeanClasses");
        String beanName = BEAN_NAME;
        Class<?> beanClass = DruidDataSourceBeanPostProcessor.class;
        Object argument = filterBeanClasses;
        registerBeanDefinition(registry, beanName, beanClass, argument);
    }
}
