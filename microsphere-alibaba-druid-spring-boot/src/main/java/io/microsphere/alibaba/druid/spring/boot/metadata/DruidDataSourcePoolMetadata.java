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

    public DruidDataSourcePoolMetadata(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }

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

    @Override
    public Integer getActive() {
        return dataSource.getActiveCount();
    }

    @Override
    public Integer getIdle() {
        return dataSource.getMaxIdle();
    }

    @Override
    public Integer getMax() {
        return dataSource.getMaxActive();
    }

    @Override
    public Integer getMin() {
        return dataSource.getMinIdle();
    }

    @Override
    public String getValidationQuery() {
        return dataSource.getValidationQuery();
    }

    @Override
    public Boolean getDefaultAutoCommit() {
        return dataSource.isDefaultAutoCommit();
    }
}
