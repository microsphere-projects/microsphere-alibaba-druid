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

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceMBean;
import io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor;
import io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties;
import io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties.Filter;
import io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable;
import io.microsphere.alibaba.druid.spring.boot.metadata.DruidDataSourcePoolMetadata;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.context.annotation.Bean;

import static io.microsphere.alibaba.druid.spring.beans.factory.config.DruidDataSourceBeanPostProcessor.BEAN_NAME;
import static org.springframework.boot.jdbc.DataSourceUnwrapper.unwrap;

/**
 * The Auto-Configuration for Alibaba Druid
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   # application.properties
 *   microsphere.alibaba.druid.enabled=true
 *
 *   # The auto-configuration registers a DruidDataSourceBeanPostProcessor and
 *   # a DataSourcePoolMetadataProvider automatically when DruidDataSource is on the classpath.
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AlibabaDruidProperties
 * @since 1.0.0
 */
@ConditionalOnAlibabaDruidAvailable
@EnableConfigurationProperties(AlibabaDruidProperties.class)
public class AlibabaDruidAutoConfiguration {

    /**
     * Register a {@link DruidDataSourceBeanPostProcessor} bean configured with filter classes
     * from {@link AlibabaDruidProperties}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Registered automatically by Spring Boot auto-configuration
     *   // Adds configured Filter beans to every DruidDataSource bean before initialization
     * }</pre>
     *
     * @param alibabaDruidProperties the Alibaba Druid properties
     * @return the configured {@link DruidDataSourceBeanPostProcessor}
     */
    @Bean(BEAN_NAME)
    public BeanPostProcessor druidDataSourceBeanPostProcessor(AlibabaDruidProperties alibabaDruidProperties) {
        Filter filter = alibabaDruidProperties.getFilter();
        return new DruidDataSourceBeanPostProcessor(filter.getClasses());
    }

    /**
     * Register a {@link DataSourcePoolMetadataProvider} that unwraps a {@link DruidDataSource} and
     * returns a {@link DruidDataSourcePoolMetadata}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Registered automatically when a DruidDataSource bean is present
     *   // Enables Spring Boot Actuator pool metrics for Druid data sources
     * }</pre>
     *
     * @return the {@link DataSourcePoolMetadataProvider} for Druid
     */
    @Bean
    @ConditionalOnBean(DruidDataSource.class)
    public DataSourcePoolMetadataProvider druidDataSourcePoolMetadataProvider() {
        return dataSource -> {
            DruidDataSource druidDataSource = unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class);
            return druidDataSource == null ? null : new DruidDataSourcePoolMetadata(druidDataSource);
        };
    }
}
