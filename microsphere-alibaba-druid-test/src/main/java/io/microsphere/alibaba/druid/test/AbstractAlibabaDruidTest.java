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
package io.microsphere.alibaba.druid.test;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.lang.function.ThrowableConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static java.sql.Statement.NO_GENERATED_KEYS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Abstract Test for Alibaba Druid
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Filter
 * @since 1.0.0
 */
public abstract class AbstractAlibabaDruidTest {

    private DruidDataSource dataSource;

    @BeforeEach
    public void init() throws Throwable {
        DruidDataSource dataSource = buildDruidDataSource();
        customize(dataSource);
        dataSource.init();
        this.dataSource = dataSource;
        initData();
    }

    /**
     * Build an instance of {@link DruidDataSource}
     *
     * @return non-null
     */
    protected DruidDataSource buildDruidDataSource() {
        return createDruidDataSource();
    }

    /**
     * Customize the {@link DruidDataSource}
     *
     * @param dataSource the {@link DruidDataSource}
     */
    protected void customize(DruidDataSource dataSource) {
    }

    /**
     * Get the {@link DataSource}
     *
     * @return non-null
     */
    protected DruidDataSource getDruidDataSource() {
        return this.dataSource;
    }

    private void initData() throws Throwable {
        executeStatement(statement -> {
            statement.execute("CREATE TABLE users (id INT, name VARCHAR(50))", NO_GENERATED_KEYS);
        });
    }

    @Test
    public void test() throws Throwable {
        testExecuteStatement();
        testExecutePreparedStatement();
    }

    protected void testExecuteStatement() throws Throwable {
        executeStatement(statement -> {
            assertEquals(1, statement.executeUpdate("INSERT INTO users (id, name) VALUES (1, 'Mercy')"));
            assertEquals(1, statement.executeUpdate("INSERT INTO users (id, name) VALUES (2, 'Blitz')", new String[0]));
            assertEquals(1, statement.executeUpdate("INSERT INTO users (id, name) VALUES (3, 'Ma')", new int[0]));
            assertEquals(1, statement.executeUpdate("INSERT INTO users (id, name) VALUES (4, 'M')", NO_GENERATED_KEYS));
            statement.addBatch("INSERT INTO users (id, name) VALUES (5, 'Z')");
            statement.addBatch("UPDATE users set name = 'z' WHERE id = 5");
            assertArrayEquals(new int[]{1, 1}, statement.executeBatch());
            ResultSet resultSet = statement.executeQuery("SELECT id,name FROM users");
            assertNotNull(resultSet);
            assertEquals(1, statement.executeUpdate("UPDATE users set name = 'Z' WHERE id = 5"));
            statement.execute("DELETE FROM users WHERE id = 1");
            statement.execute("DELETE FROM users WHERE id = 2", new String[0]);
            statement.execute("DELETE FROM users WHERE id = 3", new int[0]);
            statement.execute("DELETE FROM users WHERE id = 4", NO_GENERATED_KEYS);
            statement.execute("DELETE FROM users");
        });
    }

    protected void testExecutePreparedStatement() throws Throwable {
        executePreparedStatement("INSERT INTO users (id, name) VALUES (?, ?)", preparedStatement -> {
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, "Mercy");
            assertEquals(1, preparedStatement.executeUpdate());
        });

        executeConnection(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id,name FROM users WHERE id=?", TYPE_FORWARD_ONLY);
            preparedStatement.setInt(1, 1);
            assertNotNull(preparedStatement.executeQuery());

            preparedStatement = connection.prepareStatement("SELECT id,name FROM users WHERE id=?", TYPE_FORWARD_ONLY, CONCUR_UPDATABLE);
            preparedStatement.setInt(1, 1);
            assertNotNull(preparedStatement.execute());

        });
    }

    protected void executePreparedStatement(String sql, ThrowableConsumer<PreparedStatement> consumer) throws Throwable {
        executeConnection(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                consumer.accept(preparedStatement);
            } finally {
                preparedStatement.close();
            }
        });
    }

    protected void executeStatement(ThrowableConsumer<Statement> consumer) throws Throwable {
        executeConnection(connection -> {
            Statement statement = connection.createStatement();
            try {
                consumer.accept(statement);
            } finally {
                statement.close();
            }
        });
    }

    protected void executeConnection(ThrowableConsumer<Connection> consumer) throws Throwable {
        DataSource dataSource = getDruidDataSource();
        Connection connection = dataSource.getConnection();
        try {
            consumer.accept(connection);
        } finally {
            connection.close();
        }
    }

    @AfterEach
    public void destroy() throws Throwable {
        destroyData();
        DruidDataSource dataSource = getDruidDataSource();
        dataSource.close();
    }

    private void destroyData() throws Throwable {
        executeStatement(statement -> {
            statement.execute("DROP TABLE users");
        });
    }

    public static DruidDataSource createDruidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:h2:mem:test_mem");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("sa");
        return dataSource;
    }
}
