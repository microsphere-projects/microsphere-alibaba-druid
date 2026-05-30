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
import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.spring.beans.BeanSource;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.spring.beans.BeanSource.BEAN_FACTORY;
import static io.microsphere.spring.beans.BeanSource.SPRING_FACTORIES;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The Spring annotation enables the features of Alibaba Druid.
 * <p>
 * The {@link Filter} implementations will be initialized and added into {@link DruidDataSource} if they are defined in
 * Spring Factories config {@code META-INF/spring.factories}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @Configuration
 *   @EnableAlibabaDruid(filterClasses = LoggingStatementFilter.class)
 *   public class AppConfig {
 *
 *       @Bean(initMethod = "init", destroyMethod = "close")
 *       public DruidDataSource dataSource() {
 *           DruidDataSource ds = new DruidDataSource();
 *           ds.setUrl("jdbc:h2:mem:test");
 *           ds.setDriverClassName("org.h2.Driver");
 *           return ds;
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AlibabaDruidRegistrar
 * @since 1.0.0
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(AlibabaDruidRegistrar.class)
public @interface EnableAlibabaDruid {

    /**
     * The classes of {@link Filter} are searched in the {@link #sources() scopes}, whose instances will be registered
     * as the Spring Beans, and then be added into {@link {@link DruidDataSource#getProxyFilters() DruidDataSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @EnableAlibabaDruid(filterClasses = { LoggingStatementFilter.class })
     *   public class AppConfig { }
     * }</pre>
     *
     * @return The default value is the class of {@link Filter}, it indicates all searched
     * {@link Filter filters} should be added.
     */
    Class<? extends Filter>[] filterClasses() default {Filter.class};

    /**
     * The sources to search the instance of {@link Filter Filters} by {@link #filterClasses() the specified types}.
     *
     * @return The default value is {@code {BEAN_FACTORY, SPRING_FACTORIES}},
     * it indicates to search in Spring Bean Factory and Spring Factories config.
     */
    BeanSource[] sources() default {BEAN_FACTORY, SPRING_FACTORIES};
}