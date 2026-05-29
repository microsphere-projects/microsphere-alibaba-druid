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
import io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidRuntime;
import io.microsphere.alibaba.druid.test.junit.jupiter.AlibabaDruidTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.Integer.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DruidDataSourcePoolMetadata} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DruidDataSourcePoolMetadata
 * @see DruidDataSource
 * @since 1.0.0
 */
@AlibabaDruidTest
class DruidDataSourcePoolMetadataTest {

    @AlibabaDruidRuntime
    private DruidDataSource druidDataSource;

    private DruidDataSourcePoolMetadata metadata;

    @BeforeEach
    void setUp() {
        this.metadata = new DruidDataSourcePoolMetadata(this.druidDataSource);
    }

    @Test
    void test() {
        assertNotNull(this.metadata.getUsage());
        assertEquals(valueOf(0), this.metadata.getActive());
        assertEquals(valueOf(8), this.metadata.getIdle());
        assertEquals(valueOf(8), this.metadata.getMax());
        assertEquals(valueOf(0), this.metadata.getMin());
        assertNull(this.metadata.getValidationQuery());
        assertTrue(this.metadata.getDefaultAutoCommit());
    }
}
