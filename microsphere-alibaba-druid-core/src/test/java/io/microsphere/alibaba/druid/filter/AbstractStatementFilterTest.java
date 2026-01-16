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

package io.microsphere.alibaba.druid.filter;


import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AbstractStatementFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractStatementFilter
 * @since 1.0.0
 */
class AbstractStatementFilterTest {

    private AbstractStatementFilter filter;

    @BeforeEach
    void setUp() {
        this.filter = new AbstractStatementFilter() {
            @Override
            protected void beforeExecute(StatementProxy statement, String resourceName) throws Throwable {
            }

            @Override
            protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {
            }
        };
    }

    @Test
    void testInitOnMockDataSource() {
        DataSourceProxy dataSourceProxy = (DataSourceProxy) newProxyInstance(getSystemClassLoader(), ofArray(DataSourceProxy.class), (proxy, method, args) -> null);
        this.filter.init(dataSourceProxy);

        assertSame(dataSourceProxy, this.filter.dataSource);
        assertNull(this.filter.validationSQL);
    }

    @Test
    void testExecuteOnFailed() {
        StatementProxy statementProxy = (StatementProxy) newProxyInstance(getSystemClassLoader(), ofArray(StatementProxy.class), (proxy, method, args) -> null);

        assertThrows(SQLException.class, () -> this.filter.execute(statementProxy, () -> {
            throw new RuntimeException("For testing");
        }));
    }

    @Test
    void testBuildResourceNameOnNullPointerException() {
        SQLStatement sqlSelectStatement = new SQLSelectStatement();
        assertNull(this.filter.buildResourceName(sqlSelectStatement));
    }
}