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
package io.microsphere.alibaba.druid.spring.cloud.autoconfigure;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.alibaba.druid.filter.LoggingStatementFilter;
import io.microsphere.alibaba.druid.spring.cloud.autoconfigure.AlibabaDruidCloudAutoConfiguration.FeaturesConfiguration;
import io.microsphere.alibaba.druid.test.spring.AbstractDruidSpringTest;
import io.microsphere.alibaba.druid.test.spring.DruidDataSourceTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AlibabaDruidCloudAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AlibabaDruidCloudAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        AlibabaDruidCloudAutoConfigurationTest.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "microsphere.alibaba.druid.enabled=true",
                "spring.cloud.features.enabled=true",
                "microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter"
        })
@EnableAutoConfiguration
public class AlibabaDruidCloudAutoConfigurationTest extends AbstractDruidSpringTest {

    @Autowired
    private Map<String, HasFeatures> hasFeaturesMap;

    @Autowired
    private FeaturesConfiguration featuresConfiguration;

    @Test
    public void test() {
        assertTrue(this.hasFeaturesMap.size() > 0);
        HasFeatures hadFeatures = this.hasFeaturesMap.get("alibabaDruidFeatures");
        assertNotNull(hadFeatures);

        List<NamedFeature> namedFeatures = hadFeatures.getNamedFeatures();
        assertEquals(2, namedFeatures.size());

        assertNamedFeature(namedFeatures, 0, "microsphere.alibaba.druid.DruidDataSource", DruidDataSource.class);
        assertNamedFeature(namedFeatures, 1, "microsphere.alibaba.druid.Filter", Filter.class);

        HasFeatures hasFeatures = this.featuresConfiguration.alibabaDruidFeatures(new DefaultListableBeanFactory());
        assertTrue(hasFeatures.getAbstractFeatures().isEmpty());
        assertTrue(hasFeatures.getNamedFeatures().isEmpty());
    }

    private void assertNamedFeature(List<NamedFeature> namedFeatures, int index, String name, Class<?> type) {
        NamedFeature namedFeature = namedFeatures.get(index);
        assertEquals(name, namedFeature.getName());
        assertEquals(type, namedFeature.getType());
    }
}
