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
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * annotation is used in conjunction with the {@link AlibabaDruidTest} annotation to inject one of
 * Alibaba Druid's runtime component into test class fields or method parameters, the injection supports are as below:
 *
 * <table>
 *     <tr>
 *         <th>Component</th>
 *         <th>Static Field</th>
 *         <th>Instance Field</th>
 *         <th>Method Parameter</th>
 *     </tr>
 *     <tr>
 *         <td>{@link DataSource}</td>
 *         <td align="center">Y</td>
 *         <td align="center">Y</td>
 *         <td align="center">Y</td>
 *     </tr>
 *     <tr>
 *         <td>{@link ConnectionPoolDataSource}</td>
 *         <td align="center">Y</td>
 *         <td align="center">Y</td>
 *         <td align="center">Y</td>
 *     </tr>
 *     <tr>
 *         <td>{@link DataSourceProxy}</td>
 *         <td align="center">Y</td>
 *         <td align="center">Y</td>
 *         <td align="center">Y</td>
 *     </tr>
 *     <tr>
 *         <td>{@link DruidDataSource}</td>
 *         <td align="center">Y</td>
 *         <td align="center">Y</td>
 *         <td align="center">Y</td>
 *     </tr>
 * </table>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @AlibabaDruidTest
 *   class MyTest {
 *
 *       // Instance field injection
 *       @AlibabaDruidRuntime
 *       private DruidDataSource druidDataSource;
 *
 *       // Static field injection
 *       @AlibabaDruidRuntime
 *       private static DataSource dataSource;
 *
 *       // Method parameter injection
 *       @Test
 *       void test(@AlibabaDruidRuntime DruidDataSource ds) {
 *           assertNotNull(ds);
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AlibabaDruidTest
 * @see DataSourceProxy
 * @see DruidDataSource
 * @since 1.0.0
 */
@Target(value = {FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface AlibabaDruidRuntime {
}