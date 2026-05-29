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

package io.microsphere.alibaba.druid.test.junit.jupiter;

import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTestExtension.close;
import static io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTestExtension.getAlibabaDruidTest;
import static io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTestExtension.isCandidateParameter;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link AlibabaDruidTestExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AlibabaDruidTestExtension
 * @since 1.0.0
 */
@AlibabaDruidTest
class AlibabaDruidTestExtensionTest {

    @Nested
    @DisplayName("Test : Field Injection")
    class FieldTest extends AbstractAlibabaDruidTest {

        @AlibabaDruidRuntime
        private DruidDataSource druidDataSource;

        @AlibabaDruidRuntime
        private DataSource dataSource;

        @Override
        protected DruidDataSource buildDruidDataSource() throws IOException {
            return this.druidDataSource;
        }
    }

    @Nested
    @DisplayName("Test : Parameter Injection")
    class ParameterTest {

        @Test
        void test(@AlibabaDruidRuntime DruidDataSource druidDataSource, @AlibabaDruidRuntime DataSource dataSource) {
            assertObjects(druidDataSource, dataSource);
        }
    }

    @AlibabaDruidRuntime
    private static DruidDataSource druidDataSource;

    @AlibabaDruidRuntime
    private static DataSource dataSource;

    @Test
    @DisplayName("Test : Static Field Injection")
    void test() {
        assertObjects(druidDataSource, dataSource);
        close(druidDataSource);
        close(druidDataSource);
    }

    @Test
    void testIsCandidateParameter() throws NoSuchMethodException {
        Method method = Object.class.getMethod("equals", Object.class);
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            assertFalse(isCandidateParameter(parameter));
        }
    }

    @Test
    void testGetAlibabaDruidTest() {
        assertNotNull(getAlibabaDruidTest(AlibabaDruidTestExtensionTest.class));
    }

    void assertObjects(Object... objects) {
        for (Object object : objects) {
            assertNotNull(object);
        }
    }
}
