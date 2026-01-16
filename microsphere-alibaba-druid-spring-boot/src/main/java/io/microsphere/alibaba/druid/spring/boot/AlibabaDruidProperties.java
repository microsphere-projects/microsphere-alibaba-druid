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

import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.microsphere.alibaba.druid.constants.PropertyConstants.ALIBABA_DRUID_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ArrayUtils.of;

/**
 * The Spring Boot Properties class for Alibaba Druid
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationProperties
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = ALIBABA_DRUID_PROPERTY_NAME_PREFIX)
public class AlibabaDruidProperties {

    private boolean enabled = true;

    private Filter filter = new Filter();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * The nested Properties class for Alibaba Druid {@link com.alibaba.druid.filter.Filter}
     */
    public static class Filter {

        private Class<? extends com.alibaba.druid.filter.Filter>[] classes = of(com.alibaba.druid.filter.Filter.class);

        public Class<? extends com.alibaba.druid.filter.Filter>[] getClasses() {
            return classes;
        }

        public void setClasses(Class<? extends com.alibaba.druid.filter.Filter>[] classes) {
            this.classes = classes;
        }
    }
}
