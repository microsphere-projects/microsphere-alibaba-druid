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
import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import io.microsphere.lang.function.ThrowableSupplier;
import io.microsphere.logging.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static com.alibaba.druid.sql.SQLUtils.parseStatements;
import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ExceptionUtils.wrap;

/**
 * Abstract template class of Druid's {@link Filter} intercepts {@link StatementProxy}.
 * <p>
 * The implementation class has two scenarios mainly:
 * <ul>
 *     <li>One-Time Operations : {@link #execute(StatementProxy, ThrowableSupplier)} should be overridden.
 *     </li>
 *     <li>Two-Phase Operations : {@link #beforeExecute(StatementProxy, String)} and
 *     {@link #afterExecute(StatementProxy, String, Object, Throwable)} should be overridden.
 *     </li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Create a custom filter by extending AbstractStatementFilter
 *   public class MyStatementFilter extends AbstractStatementFilter {
 *
 *       @Override
 *       protected void beforeExecute(StatementProxy statement, String resourceName) {
 *           System.out.println("Before executing: " + resourceName);
 *       }
 *
 *       @Override
 *       protected void afterExecute(StatementProxy statement, String resourceName,
 *                                   Object result, Throwable failure) {
 *           System.out.println("After executing: " + resourceName + ", result: " + result);
 *       }
 *   }
 *
 *   // Register the filter with a DruidDataSource
 *   DruidDataSource dataSource = new DruidDataSource();
 *   dataSource.getProxyFilters().add(new MyStatementFilter());
 *   dataSource.init();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Filter
 * @see FilterAdapter
 * @see StatementProxy
 * @since 1.0.0
 */
public abstract class AbstractStatementFilter extends FilterAdapter {

    protected final Logger logger = getLogger(getClass());

    protected DataSourceProxy dataSource;

    protected String validationSQL;

    /**
     * Initialize this filter with the given {@link DataSourceProxy}.
     * Extracts and caches the validation SQL query from the data source if it is a {@link DruidDataSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSource druidDataSource = new DruidDataSource();
     *   druidDataSource.setValidationQuery("SELECT 1");
     *   druidDataSource.getProxyFilters().add(new MyStatementFilter());
     *   // init() is called internally by DruidDataSource during initialization
     *   druidDataSource.init();
     * }</pre>
     *
     * @param dataSource the {@link DataSourceProxy} to initialize with
     */
    @Override
    public final void init(DataSourceProxy dataSource) {
        this.dataSource = dataSource;
        if (dataSource instanceof DruidDataSource) {
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            this.validationSQL = druidDataSource.getValidationQuery();
        }
        if (logger.isTraceEnabled()) {
            logger.trace("DataSourceProxy({}) was initialized with validation SQL : {}", this.dataSource, this.validationSQL);
        }
    }

    @Override
    public final boolean preparedStatement_execute(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
        return execute(statement, () -> super.preparedStatement_execute(chain, statement));
    }

    @Override
    public final ResultSetProxy preparedStatement_executeQuery(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
        return execute(statement, () -> super.preparedStatement_executeQuery(chain, statement));
    }

    @Override
    public final int preparedStatement_executeUpdate(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
        return execute(statement, () -> super.preparedStatement_executeUpdate(chain, statement));
    }

    @Override
    public final ResultSetProxy statement_executeQuery(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        return execute(statement, () -> super.statement_executeQuery(chain, statement, sql));
    }

    @Override
    public final boolean statement_execute(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        return execute(statement, () -> super.statement_execute(chain, statement, sql));
    }

    @Override
    public final boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys) throws SQLException {
        return execute(statement, () -> super.statement_execute(chain, statement, sql, autoGeneratedKeys));
    }

    @Override
    public final boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int[] columnIndexes) throws SQLException {
        return execute(statement, () -> super.statement_execute(chain, statement, sql, columnIndexes));
    }

    @Override
    public final boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, String[] columnNames) throws SQLException {
        return execute(statement, () -> super.statement_execute(chain, statement, sql, columnNames));
    }

    @Override
    public final int[] statement_executeBatch(FilterChain chain, StatementProxy statement) throws SQLException {
        return execute(statement, () -> super.statement_executeBatch(chain, statement));
    }

    @Override
    public final int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        return execute(statement, () -> super.statement_executeUpdate(chain, statement, sql));
    }

    @Override
    public final int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys) throws SQLException {
        return execute(statement, () -> super.statement_executeUpdate(chain, statement, sql, autoGeneratedKeys));
    }

    @Override
    public final int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int[] columnIndexes) throws SQLException {
        return execute(statement, () -> super.statement_executeUpdate(chain, statement, sql, columnIndexes));
    }

    @Override
    public final int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, String[] columnNames) throws SQLException {
        return execute(statement, () -> super.statement_executeUpdate(chain, statement, sql, columnNames));
    }

    /**
     * Execute the given {@link ThrowableSupplier callback} for the provided {@link StatementProxy},
     * invoking {@link #beforeExecute} and {@link #afterExecute} around the actual execution.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically called internally; subclasses may override to customize execution
     *   T result = execute(statement, () -> chain.preparedStatement_execute(statement));
     * }</pre>
     *
     * @param statement the {@link StatementProxy} being executed
     * @param callback  the {@link ThrowableSupplier} that performs the actual JDBC operation
     * @param <T>       the result type
     * @return the result of the callback
     * @throws SQLException if the callback throws or wraps any exception
     */
    protected <T> T execute(StatementProxy statement, ThrowableSupplier<T> callback) throws SQLException {
        String resourceName = buildResourceName(statement);
        T result = null;
        Throwable failure = null;
        try {
            beforeExecute(statement, resourceName);
            result = callback.get();
        } catch (Throwable e) {
            failure = e;
            throw wrap(e, SQLException.class);
        } finally {
            if (logger.isTraceEnabled()) {
                logger.trace("Execute statement [value : {} , resource name : '{}'] : {}", statement.getLastExecuteSql(), resourceName, result, failure);
            }
            afterExecute(statement, resourceName, result, failure);
        }
        return result;
    }

    /**
     * Execute before call of intercepted method.
     *
     * @param statement    {@link StatementProxy}
     * @param resourceName the resource name
     * @throws Throwable if any error
     */
    protected abstract void beforeExecute(StatementProxy statement, String resourceName) throws Throwable;

    /**
     * Execute after call of intercepted method.
     *
     * @param statement    {@link StatementProxy}
     * @param resourceName the resource name
     * @param result       the result of intercepted method
     * @param failure      the failure of intercepted method
     */
    protected abstract void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure);

    /**
     * Build resource name
     *
     * @param statement {@link StatementProxy}
     * @return non-null
     */
    protected String buildResourceName(StatementProxy statement) {
        String sql = statement.getLastExecuteSql();
        if (Objects.equals(sql, validationSQL)) {
            return sql;
        }
        String dbType = dataSource.getDbType();
        List<SQLStatement> statementList = parseStatements(sql, dbType);
        SQLStatement sqlStatement = first(statementList);
        String resourceName = buildResourceName(sqlStatement);
        if (resourceName == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("The JDBC statement can't be recognized, sql : '{}' , dbType : '{}'", sql, dbType);
            }
            resourceName = "UNRECOGNIZED";
        }
        return resourceName;
    }

    /**
     * Build a resource name from the given {@link SQLStatement} by dispatching to the appropriate
     * overloaded method for SELECT, UPDATE, INSERT, or DELETE statement types.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   List<SQLStatement> stmts = SQLUtils.parseStatements("SELECT id FROM users", "h2");
     *   String name = buildResourceName(stmts.get(0));
     *   // name == "SELECT users"
     * }</pre>
     *
     * @param sqlStatement the parsed {@link SQLStatement}, may be {@code null}
     * @return the resource name, or {@code null} if the statement type is unrecognized
     */
    protected String buildResourceName(SQLStatement sqlStatement) {
        try {
            if (sqlStatement instanceof SQLSelectStatement) {
                return buildResourceName((SQLSelectStatement) sqlStatement);
            } else if (sqlStatement instanceof SQLUpdateStatement) {
                return buildResourceName((SQLUpdateStatement) sqlStatement);
            } else if (sqlStatement instanceof SQLInsertStatement) {
                return buildResourceName((SQLInsertStatement) sqlStatement);
            } else if (sqlStatement instanceof SQLDeleteStatement) {
                return buildResourceName((SQLDeleteStatement) sqlStatement);
            }
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("The JDBC statement can't be parsed, sql : '{}'", sqlStatement, e);
            }
        }
        return null;
    }

    /**
     * Build the resource name for a {@link SQLSelectStatement}, in the form {@code "SELECT <table>"}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // For SQL: "SELECT id, name FROM users"
     *   // returns: "SELECT users"
     * }</pre>
     *
     * @param selectStatement the {@link SQLSelectStatement}
     * @return the resource name
     */
    protected String buildResourceName(SQLSelectStatement selectStatement) {
        SQLSelect sqlSelect = selectStatement.getSelect();
        SQLSelectQueryBlock sqlSelectQueryBlock = sqlSelect.getFirstQueryBlock();
        SQLTableSource sqlTableSource = sqlSelectQueryBlock.getFrom();
        return "SELECT " + sqlTableSource.computeAlias();
    }

    /**
     * Build the resource name for a {@link SQLUpdateStatement}, in the form {@code "UPDATE <table>"}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // For SQL: "UPDATE users SET name = 'Z' WHERE id = 5"
     *   // returns: "UPDATE users"
     * }</pre>
     *
     * @param updateStatement the {@link SQLUpdateStatement}
     * @return the resource name
     */
    protected String buildResourceName(SQLUpdateStatement updateStatement) {
        SQLTableSource sqlTableSource = updateStatement.getTableSource();
        return "UPDATE " + sqlTableSource.computeAlias();
    }

    /**
     * Build the resource name for a {@link SQLInsertStatement}, in the form {@code "INSERT <table>"}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // For SQL: "INSERT INTO users (id, name) VALUES (1, 'Mercy')"
     *   // returns: "INSERT users"
     * }</pre>
     *
     * @param insertStatement the {@link SQLInsertStatement}
     * @return the resource name
     */
    protected String buildResourceName(SQLInsertStatement insertStatement) {
        SQLExprTableSource sqlTableSource = insertStatement.getTableSource();
        return "INSERT " + sqlTableSource.computeAlias();
    }

    /**
     * Build the resource name for a {@link SQLDeleteStatement}, in the form {@code "DELETE <table>"}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // For SQL: "DELETE FROM users WHERE id = 1"
     *   // returns: "DELETE users"
     * }</pre>
     *
     * @param deleteStatement the {@link SQLDeleteStatement}
     * @return the resource name
     */
    protected String buildResourceName(SQLDeleteStatement deleteStatement) {
        SQLTableSource sqlTableSource = deleteStatement.getTableSource();
        return "DELETE " + sqlTableSource.computeAlias();
    }
}
