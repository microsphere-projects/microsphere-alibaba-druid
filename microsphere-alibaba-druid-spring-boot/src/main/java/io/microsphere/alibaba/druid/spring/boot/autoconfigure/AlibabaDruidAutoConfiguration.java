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
package io.microsphere.alibaba.druid.spring.boot.autoconfigure;

import io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor;
import io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties.PREFIX;
import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;

/**
 * The Auto-Configuration for Alibaba Druid
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AlibabaDruidProperties
 * @since 1.0.0
 */
@ConditionalOnProperty(prefix = PREFIX, name = ENABLED_PROPERTY_NAME, matchIfMissing = true)
@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")
@EnableConfigurationProperties(AlibabaDruidProperties.class)
public class AlibabaDruidAutoConfiguration {

    @Bean
    public BeanPostProcessor druidDataSourceBeanPostProcessor(AlibabaDruidProperties alibabaDruidProperties) {
        AlibabaDruidProperties.Filter filter = alibabaDruidProperties.getFilter();
        return new DruidDataSourceBeanPostProcessor(filter.getClasses());
    }
}
