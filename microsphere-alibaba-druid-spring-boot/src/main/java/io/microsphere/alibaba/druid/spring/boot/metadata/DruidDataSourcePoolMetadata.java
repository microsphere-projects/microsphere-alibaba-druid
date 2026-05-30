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
package io.microsphere.alibaba.druid.spring.boot.metadata;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadata;

/**
 * Druid {@link DataSourcePoolMetadata}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @AlibabaDruidTest
 *   class MyTest {
 *
 *       @AlibabaDruidRuntime
 *       private DruidDataSource druidDataSource;
 *
 *       @Test
 *       void testMetadata() {
 *           DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
 *           assertNotNull(metadata.getUsage());
 *           assertEquals(Integer.valueOf(0), metadata.getActive());
 *           assertEquals(Integer.valueOf(8), metadata.getMax());
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DataSourcePoolMetadata
 * @see DruidDataSource
 * @since 1.0.0
 */
public class DruidDataSourcePoolMetadata implements DataSourcePoolMetadata {

    private static final float ZERO = 0.0f;
    private static final float ONE = 1.0f;

    private final DruidDataSource dataSource;

    /**
     * Create a new {@link DruidDataSourcePoolMetadata} for the given {@link DruidDataSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
     *   assertNotNull(metadata.getUsage());
     * }</pre>
     *
     * @param dataSource the {@link DruidDataSource} to expose pool metadata for
     */
    public DruidDataSourcePoolMetadata(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Return the usage ratio of the connection pool: {@code 0.0} when no connections are active,
     * {@code 1.0} when all connections are in use, otherwise a fraction.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
     *   Float usage = metadata.getUsage();
     *   // usage == 0.0f when no connections are active
     * }</pre>
     *
     * @return the pool usage ratio, never {@code null}
     */
    @Override
    public Float getUsage() {
        int active = dataSource.getActiveCount();
        if (active == 0) {
            return ZERO;
        } else {
            int maxActive = dataSource.getMaxActive();
            if (active == maxActive) {
                return ONE;
            }
            return active / (maxActive * ONE);
        }
    }

    /**
     * Return the number of active (in-use) connections in the pool.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
     *   assertEquals(Integer.valueOf(0), metadata.getActive());
     * }</pre>
     *
     * @return the active connection count
     */
    @Override
    public Integer getActive() {
        return dataSource.getActiveCount();
    }

    /**
     * Return the maximum number of idle connections allowed in the pool.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
     *   assertEquals(Integer.valueOf(8), metadata.getIdle());
     * }</pre>
     *
     * @return the maximum idle connection count
     */
    @Override
    public Integer getIdle() {
        return dataSource.getMaxIdle();
    }

    /**
     * Return the maximum number of active connections the pool can provide.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
     *   assertEquals(Integer.valueOf(8), metadata.getMax());
     * }</pre>
     *
     * @return the maximum active connection count
     */
    @Override
    public Integer getMax() {
        return dataSource.getMaxActive();
    }

    /**
     * Return the minimum number of idle connections the pool tries to maintain.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
     *   assertEquals(Integer.valueOf(0), metadata.getMin());
     * }</pre>
     *
     * @return the minimum idle connection count
     */
    @Override
    public Integer getMin() {
        return dataSource.getMinIdle();
    }

    /**
     * Return the validation query used to test connections in the pool.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
     *   // Returns null if no validation query is configured (H2 default)
     *   assertNull(metadata.getValidationQuery());
     * }</pre>
     *
     * @return the validation query string, or {@code null} if not configured
     */
    @Override
    public String getValidationQuery() {
        return dataSource.getValidationQuery();
    }

    /**
     * Return whether connections are auto-committed by default.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourcePoolMetadata metadata = new DruidDataSourcePoolMetadata(druidDataSource);
     *   assertTrue(metadata.getDefaultAutoCommit());
     * }</pre>
     *
     * @return {@code true} if auto-commit is enabled by default
     */
    @Override
    public Boolean getDefaultAutoCommit() {
        return dataSource.isDefaultAutoCommit();
    }
}
