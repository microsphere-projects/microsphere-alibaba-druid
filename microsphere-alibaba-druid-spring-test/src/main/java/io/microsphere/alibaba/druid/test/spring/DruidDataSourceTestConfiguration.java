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
package io.microsphere.alibaba.druid.test.spring;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.io.IOException;

import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.buildDefaultDruidDataSource;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * {@link DruidDataSource} Test Configuration
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @ExtendWith(SpringExtension.class)
 *   @ContextConfiguration(classes = {
 *       DruidDataSourceTestConfiguration.class,
 *       LoggingStatementFilter.class,
 *   })
 *   public class MyTest extends AbstractDruidSpringTest {
 *       // DruidDataSourceTestConfiguration provides the DruidDataSource bean
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DruidDataSource
 * @since 1.0.0
 */
public class DruidDataSourceTestConfiguration {

    /**
     * Create a prototype-scoped {@link DruidDataSource} bean configured from the default H2 properties.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Registered automatically when DruidDataSourceTestConfiguration is imported
     *   // Each call returns a new DruidDataSource configured from META-INF/druid/h2.properties
     *   DruidDataSource ds = beanFactory.getBean(DruidDataSource.class);
     *   assertNotNull(ds);
     * }</pre>
     *
     * @return a newly-configured {@link DruidDataSource}
     * @throws IOException if properties cannot be loaded
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    @Scope(SCOPE_PROTOTYPE)
    public DruidDataSource dataSource() throws IOException {
        return buildDefaultDruidDataSource();
    }
}
