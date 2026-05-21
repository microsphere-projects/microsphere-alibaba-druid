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
import io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

/**
 * Abstract Spring Test for Alibaba Druid
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @ExtendWith(SpringExtension.class)
 *   @ContextConfiguration(classes = {
 *       LoggingStatementFilter.class,
 *       DruidDataSourceTestConfiguration.class,
 *       MySpringTest.class,
 *   })
 *   @EnableAlibabaDruid(filterBeanClasses = LoggingStatementFilter.class)
 *   public class MySpringTest extends AbstractDruidSpringTest {
 *       // All AbstractAlibabaDruidTest tests run with a Spring-managed DruidDataSource
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DataSource
 * @see DruidDataSource
 * @since 1.0.0
 */
public abstract class AbstractDruidSpringTest extends AbstractAlibabaDruidTest {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * Build the {@link DruidDataSource} by retrieving it from the Spring {@link BeanFactory}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // The DruidDataSource bean must be registered in the Spring context
     *   // (e.g., via DruidDataSourceTestConfiguration)
     *   DruidDataSource dataSource = buildDruidDataSource();
     *   assertNotNull(dataSource);
     * }</pre>
     *
     * @return the {@link DruidDataSource} bean from the Spring context
     */
    protected DruidDataSource buildDruidDataSource() {
        return beanFactory.getBean(DruidDataSource.class);
    }

}
