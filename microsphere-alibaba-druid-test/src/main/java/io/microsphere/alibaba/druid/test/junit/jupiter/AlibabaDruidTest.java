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

package io.microsphere.alibaba.druid.test.junit.jupiter;

import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Properties;

import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.DEFAULT_PROPERTIES_RESOURCE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link AlibabaDruidTest} is a JUnit Jupiter extension to setup {@link AlibabaDruidRuntime Alibaba Druid runtime}
 * used in test cases.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AlibabaDruidRuntime
 * @see AlibabaDruidTestExtension
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@ExtendWith(AlibabaDruidTestExtension.class)
@Inherited
public @interface AlibabaDruidTest {

    /**
     * The properties of {@link DruidDataSource} in key-value format.
     * <h3>Example Usage</h3>
     * <pre>{@code
     *
     * @AlibabaDruidTest(
     *  properties= {
     *      "druid.name=mysql",
     *      "druid.url=jdbc:mysql://localhost:3306/test",
     *      "druid.username=root",
     *      "druid.password=123456",
     *      "druid.driverClassName=com.mysql.jdbc.Driver"
     *    }
     * )
     * class MyTest {
     * }
     * }</prev>
     * <p>
     * Current method can override the properties defined in {@link #propertiesResource()}.
     * </p>
     *
     * @return empty array as default
     * @see DruidDataSource#configFromProperties(Properties)
     */
    String[] properties() default {};

    /**
     * The properties resource of {@link DruidDataSource} in key-value format.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * @AlibabaDruidTest(propertiesResource="/META-INF/druid/h2.properties")
     * class MyTest {
     * }
     * }
     * </pre>
     * <p>
     * The content of resource "/META-INF/druid/h2.properties" :
     * <pre>{@code
     * druid.name=h2
     * druid.url=jdbc:h2:mem:test_mem
     * druid.username=sa
     * druid.password=
     * druid.driverClassName=org.h2.Driver
     * }</pre>
     *
     * @return {@link AlibabaDruidTestUtils#DEFAULT_PROPERTIES_RESOURCE} as default
     * @see DruidDataSource#configFromProperties(Properties)
     */
    String propertiesResource() default DEFAULT_PROPERTIES_RESOURCE;

    /**
     * The names of filters to be loaded for {@link DruidDataSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @AlibabaDruidTest(
     *  filters= {
     *      "log",
     *      "stat",
     *      "wall"
     *    }
     * )
     * </pre>
     *
     * @return empty array as default
     * @see DruidDataSource#setFilters(String)
     */
    String[] filters() default {};
}
