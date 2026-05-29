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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.DEFAULT_PROPERTIES_RESOURCE;
import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.H2_PROPERTIES_RESOURCE;
import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.PACKAGE_NAME_PREFIX;
import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.getDefaultProperties;
import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.loadProperties;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AlibabaDruidTestUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AlibabaDruidTestUtils
 * @since 1.0.0
 */
class AlibabaDruidTestUtilsTest {

    @Test
    void testConstans() {
        assertEquals("META-INF/druid/h2.properties", H2_PROPERTIES_RESOURCE);
        assertEquals("META-INF/druid/h2.properties", DEFAULT_PROPERTIES_RESOURCE);
        assertEquals("com.alibaba.druid.", PACKAGE_NAME_PREFIX);
    }

    /**
     * <pre>{@code
     * druid.name=h2
     * druid.url=jdbc:h2:mem:test_mem
     * druid.username=sa
     * druid.password=
     * druid.driverClassName=org.h2.Driver
     * }</pre>
     */
    @Test
    void testGetDefaultProperties() throws IOException {
        Properties properties = getDefaultProperties();
        assertEquals("h2", properties.getProperty("druid.name"));
        assertEquals("jdbc:h2:mem:test_mem", properties.getProperty("druid.url"));
        assertEquals("sa", properties.getProperty("druid.username"));
        assertEquals("", properties.getProperty("druid.password"));
        assertEquals("org.h2.Driver", properties.getProperty("druid.driverClassName"));
    }

    @Test
    void testLoadProperties() throws IOException {
        Properties properties = loadProperties(ofArray("name = value"));
        assertEquals("value", properties.getProperty("name"));
    }
}
