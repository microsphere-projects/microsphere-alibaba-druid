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

import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;
import java.util.StringJoiner;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static io.microsphere.util.ClassLoaderUtils.getResource;

/**
 * The utils class of Alibaba Druid Test.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Load the default H2 properties and build a DruidDataSource
 *   Properties properties = AlibabaDruidTestUtils.getDefaultProperties();
 *   DruidDataSource dataSource = AlibabaDruidTestUtils.buildDruidDataSource(properties);
 *   dataSource.init();
 *
 *   // Or use the convenience method directly
 *   DruidDataSource defaultDataSource = AlibabaDruidTestUtils.buildDefaultDruidDataSource();
 *   defaultDataSource.init();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AlibabaDruidTestUtils {

    /**
     * The resource name of H2 Database Properties of {@link DruidDataSource} : "META-INF/druid/h2.properties"
     *
     * @see DruidDataSource#configFromProperties(Properties)
     */
    public static final String H2_PROPERTIES_RESOURCE = "META-INF/druid/h2.properties";

    /**
     * The default resource name of Propertiess
     *
     * @see DruidDataSource#configFromProperties(Properties)
     * @see #H2_PROPERTIES_RESOURCE
     */
    public static final String DEFAULT_PROPERTIES_RESOURCE = H2_PROPERTIES_RESOURCE;

    /**
     * The package name prefix of Alibaba Druid
     */
    public static final String PACKAGE_NAME_PREFIX = "com.alibaba.druid.";

    /**
     * Load {@link Properties} from the {@link #DEFAULT_PROPERTIES_RESOURCE}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Properties properties = AlibabaDruidTestUtils.getDefaultProperties();
     *   // properties.getProperty("druid.name")           == "h2"
     *   // properties.getProperty("druid.url")            == "jdbc:h2:mem:test_mem"
     *   // properties.getProperty("druid.username")       == "sa"
     *   // properties.getProperty("druid.driverClassName")== "org.h2.Driver"
     * }</pre>
     *
     * @return {@link Properties}
     * @throws IOException if any I/O error
     */
    @Nonnull
    public static Properties getDefaultProperties() throws IOException {
        return loadProperties(DEFAULT_PROPERTIES_RESOURCE);
    }

    /**
     * Load {@link Properties} from the specified resource name
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Properties properties = AlibabaDruidTestUtils.loadProperties("META-INF/druid/h2.properties");
     *   assertEquals("h2", properties.getProperty("druid.name"));
     * }</pre>
     *
     * @param resourceName resource name
     * @return {@link Properties}
     * @throws IOException if any I/O error
     */
    @Nonnull
    public static Properties loadProperties(String resourceName) throws IOException {
        final Properties properties;
        URL resource = getResource(resourceName);
        try (InputStream inputStream = resource.openStream()) {
            properties = properties(copyToString(inputStream, DEFAULT_CHARSET));
        }
        return properties;
    }

    /**
     * Load {@link Properties} from the specified key-value pairs
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Properties properties = AlibabaDruidTestUtils.loadProperties(new String[]{"name = value", "key = data"});
     *   assertEquals("value", properties.getProperty("name"));
     *   assertEquals("data", properties.getProperty("key"));
     * }</pre>
     *
     * @param keyValuePairs key-value pairs
     * @return {@link Properties}
     * @throws IOException if any I/O error
     */
    @Nonnull
    public static Properties loadProperties(String[] keyValuePairs) throws IOException {
        StringJoiner contentBuilder = new StringJoiner(LINE_SEPARATOR);
        for (String keyValuePair : keyValuePairs) {
            contentBuilder.add(keyValuePair);
        }
        return properties(contentBuilder.toString());
    }

    /**
     * Load {@link Properties} from the specified resource content
     *
     * @param content resource content
     * @return {@link Properties}
     * @throws IOException if any I/O error
     */
    @Nonnull
    static Properties properties(String content) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = new StringReader(content)) {
            properties.load(reader);
        }
        return properties;
    }

    /**
     * Build the default {@link DruidDataSource} from the {@link #getDefaultProperties()}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSource dataSource = AlibabaDruidTestUtils.buildDefaultDruidDataSource();
     *   dataSource.init();
     *   Connection connection = dataSource.getConnection();
     *   // use connection ...
     *   connection.close();
     *   dataSource.close();
     * }</pre>
     *
     * @return {@link DruidDataSource}
     * @throws RuntimeException if any I/O error or {@link NullPointerException}
     */
    @Nonnull
    public static DruidDataSource buildDefaultDruidDataSource() throws IOException {
        Properties properties = getDefaultProperties();
        return buildDruidDataSource(properties);
    }

    /**
     * Build {@link DruidDataSource} from the {@link #getDefaultProperties()}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Properties properties = AlibabaDruidTestUtils.getDefaultProperties();
     *   DruidDataSource dataSource = AlibabaDruidTestUtils.buildDruidDataSource(properties);
     *   dataSource.init();
     *   dataSource.close();
     * }</pre>
     *
     * @param properties {@link Properties}
     * @return {@link DruidDataSource}
     * @throws RuntimeException if any I/O error or {@link NullPointerException}
     */
    @Nonnull
    public static DruidDataSource buildDruidDataSource(Properties properties) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.configFromProperties(properties);
        return dataSource;
    }

    private AlibabaDruidTestUtils() {
    }
}
