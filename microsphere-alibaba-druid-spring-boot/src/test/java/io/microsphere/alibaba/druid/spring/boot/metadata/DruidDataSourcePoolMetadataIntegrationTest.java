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
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import io.microsphere.alibaba.druid.filter.AbstractStatementFilter;
import io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest;
import org.junit.jupiter.api.Test;

import static io.microsphere.collection.Lists.ofList;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DruidDataSourcePoolMetadata} Integration Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DruidDataSourcePoolMetadata
 * @since 1.0.0
 */
public class DruidDataSourcePoolMetadataIntegrationTest extends AbstractAlibabaDruidTest {

    private DruidDataSourcePoolMetadata metadata;

    @Override
    protected void customize(DruidDataSource dataSource) {

        this.metadata = new DruidDataSourcePoolMetadata(dataSource);
        AbstractStatementFilter filterAdapter = new AbstractStatementFilter() {

            @Override
            protected void beforeExecute(StatementProxy statement, String resourceName) {
                Float usage = metadata.getUsage();
                assertTrue(usage.floatValue() > 0.0f);
            }

            @Override
            protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {
                Float usage = metadata.getUsage();
                assertTrue(usage.floatValue() > 0.0f);
            }
        };
        dataSource.setProxyFilters(ofList(filterAdapter));
    }

    @Test
    @Override
    public void test() throws Throwable {
        test(1);
        test(2);
    }

    void test(int maxActive) throws Throwable {
        this.dataSource.setMaxActive(maxActive);
        super.test();
    }
}
