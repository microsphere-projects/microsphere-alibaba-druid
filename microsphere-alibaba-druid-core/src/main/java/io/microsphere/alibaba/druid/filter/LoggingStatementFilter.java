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

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;

/**
 * Logging {@link StatementProxy} {@link Filter}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Register LoggingStatementFilter with a DruidDataSource
 *   DruidDataSource dataSource = new DruidDataSource();
 *   dataSource.getProxyFilters().add(new LoggingStatementFilter());
 *   dataSource.init();
 *
 *   // All executed statements will be logged at DEBUG level
 *   Connection connection = dataSource.getConnection();
 *   Statement statement = connection.createStatement();
 *   statement.execute("SELECT id, name FROM users");
 *   statement.close();
 *   connection.close();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractStatementFilter
 * @see StatementProxy
 * @see Filter
 * @since 1.0.0
 */
public class LoggingStatementFilter extends AbstractStatementFilter {

    /**
     * Logs the SQL statement and resource name before execution at DEBUG level.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Invoked automatically before each statement execution when the filter is registered
     *   // DEBUG log: beforeExecute(statement : SELECT id,name FROM users , resource name : 'SELECT users')
     * }</pre>
     *
     * @param statement    the {@link StatementProxy} about to be executed
     * @param resourceName the resolved resource name (e.g., {@code "SELECT users"})
     * @throws Throwable if any error occurs during pre-execution
     */
    @Override
    protected void beforeExecute(StatementProxy statement, String resourceName) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("beforeExecute(statement : {} , resource name : '{}') : {}", statement.getLastExecuteSql(), resourceName);
        }
    }

    /**
     * Logs the SQL statement, resource name, result, and any failure after execution at DEBUG level.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Invoked automatically after each statement execution when the filter is registered
     *   // DEBUG log: afterExecute(statement : INSERT INTO users ... , resource name : 'INSERT users' ,
     *   //                         result : 1 , failure : null)
     * }</pre>
     *
     * @param statement    the {@link StatementProxy} that was executed
     * @param resourceName the resolved resource name (e.g., {@code "INSERT users"})
     * @param result       the result returned by the statement execution, or {@code null}
     * @param failure      the exception thrown during execution, or {@code null} if successful
     */
    @Override
    protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {
        if (logger.isDebugEnabled()) {
            logger.debug("afterExecute(statement : {} , resource name : '{}' , result : {} , failure : {})",
                    statement.getLastExecuteSql(), resourceName, result, failure);
        }
    }
}
