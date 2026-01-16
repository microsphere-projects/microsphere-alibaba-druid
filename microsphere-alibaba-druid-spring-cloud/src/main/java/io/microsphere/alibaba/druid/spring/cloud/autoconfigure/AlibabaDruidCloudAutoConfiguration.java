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
import io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties;
import io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable;
import io.microsphere.spring.cloud.client.condition.ConditionalOnFeaturesEnabled;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;

import static io.microsphere.alibaba.druid.constants.PropertyConstants.ALIBABA_DRUID_PROPERTY_NAME_PREFIX;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static java.util.Collections.emptyList;

/**
 * The Spring Cloud Auto-Configuration for Alibaba Druid
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AlibabaDruidProperties
 * @since 1.0.0
 */
@ConditionalOnAlibabaDruidAvailable
@Import(AlibabaDruidCloudAutoConfiguration.FeaturesConfiguration.class)
public class AlibabaDruidCloudAutoConfiguration {

    @ConditionalOnFeaturesEnabled
    public static class FeaturesConfiguration {

        /**
         * The bean name of {@link HasFeatures}
         *
         * @see #alibabaDruidFeatures(ListableBeanFactory)
         */
        public final static String ALIBABA_DRUID_FEATURES_BEAN_NAME = "alibabaDruidFeatures";

        // TODO : Add more features, e.g: WebStatFilter
        private static Set<Class<?>> typeFeatures = of(
                DruidDataSource.class,
                Filter.class
        );

        @Bean(name = ALIBABA_DRUID_FEATURES_BEAN_NAME)
        public HasFeatures alibabaDruidFeatures(ListableBeanFactory beanFactory) {
            List<NamedFeature> namedFeatures = newArrayList(typeFeatures.size());
            for (Class<?> type : typeFeatures) {
                if (isBeanPresent(beanFactory, type)) {
                    String name = ALIBABA_DRUID_PROPERTY_NAME_PREFIX + DOT_CHAR + type.getSimpleName();
                    namedFeatures.add(new NamedFeature(name, type));
                }
            }
            return new HasFeatures(emptyList(), namedFeatures);
        }
    }
}