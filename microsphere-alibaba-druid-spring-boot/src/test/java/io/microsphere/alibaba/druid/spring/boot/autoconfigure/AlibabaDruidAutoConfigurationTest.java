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
package io.microsphere.alibaba.druid.spring.boot.autoconfigure;

import io.microsphere.alibaba.druid.filter.LoggingStatementFilter;
import io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties;
import io.microsphere.alibaba.druid.test.spring.AbstractDruidSpringTest;
import io.microsphere.alibaba.druid.test.spring.DruidDataSourceTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static io.microsphere.util.ArrayUtils.of;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link AlibabaDruidAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AlibabaDruidAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                LoggingStatementFilter.class,
                DruidDataSourceTestConfiguration.class,
                AlibabaDruidAutoConfigurationTest.class
        },
        webEnvironment = NONE,
        properties = {
                "microsphere.alibaba.druid.enabled=true",
                "microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter"
        })
@EnableAutoConfiguration
public class AlibabaDruidAutoConfigurationTest extends AbstractDruidSpringTest {

    @Autowired
    private AlibabaDruidProperties alibabaDruidProperties;

    @Autowired
    private LoggingStatementFilter loggingStatementFilter;

    @Test
    public void test() throws Throwable {
        super.test();
        assertNotNull(alibabaDruidProperties);
        assertNotNull(loggingStatementFilter);

        AlibabaDruidProperties.Filter filter = alibabaDruidProperties.getFilter();
        assertNotNull(filter);

        Class<? extends com.alibaba.druid.filter.Filter>[] classes = filter.getClasses();
        assertNotNull(classes);
        assertArrayEquals(of(LoggingStatementFilter.class), classes);
    }
}


