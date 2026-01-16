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

package io.microsphere.alibaba.druid.spring.boot;


import io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AlibabaDruidProperties} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AlibabaDruidProperties
 * @since 1.0.0
 */
class AlibabaDruidPropertiesTest {

    private AlibabaDruidProperties alibabaDruidProperties;

    @BeforeEach
    void setUp() {
        this.alibabaDruidProperties = new AlibabaDruidProperties();
    }

    @Test
    void test() {
        assertTrue(this.alibabaDruidProperties.isEnabled());

        this.alibabaDruidProperties.setEnabled(false);
        assertFalse(this.alibabaDruidProperties.isEnabled());

        assertNotNull(this.alibabaDruidProperties.getFilter());

        Filter filter = new Filter();
        this.alibabaDruidProperties.setFilter(filter);
        assertSame(filter, this.alibabaDruidProperties.getFilter());

        assertTrue(isNotEmpty(filter.getClasses()));

        filter.setClasses(ofArray(com.alibaba.druid.filter.Filter.class));
        assertSame(filter, this.alibabaDruidProperties.getFilter());

        assertArrayEquals(ofArray(com.alibaba.druid.filter.Filter.class), filter.getClasses());
    }
}