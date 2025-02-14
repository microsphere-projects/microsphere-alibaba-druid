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
package io.microsphere.druid.filter;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;

/**
 * Logging {@link StatementProxy} {@link Filter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractStatementFilter
 * @see StatementProxy
 * @see Filter
 * @since 1.0.0
 */
public class LoggingStatementFilter extends AbstractStatementFilter {

    @Override
    protected void beforeExecute(StatementProxy statement, String resourceName) throws Throwable {
        logger.debug("beforeExecute(statement : {} , resource name : '{}') : {}", statement.getLastExecuteSql(), resourceName);
    }

    @Override
    protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {
        logger.debug("afterExecute(statement : {} , resource name : '{}' , result : {} , failure : {})",
                statement.getLastExecuteSql(), resourceName, result, failure);
    }
}
