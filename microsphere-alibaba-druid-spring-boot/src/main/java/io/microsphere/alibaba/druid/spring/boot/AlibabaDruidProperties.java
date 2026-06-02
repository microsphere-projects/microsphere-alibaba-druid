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

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.microsphere.alibaba.druid.constants.PropertyConstants.ALIBABA_DRUID_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ArrayUtils.of;

/**
 * The Spring Boot Properties class for Alibaba Druid
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   # application.properties
 *   microsphere.alibaba.druid.enabled=true
 *   microsphere.alibaba.druid.filter.classes=io.microsphere.alibaba.druid.filter.LoggingStatementFilter
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationProperties
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = ALIBABA_DRUID_PROPERTY_NAME_PREFIX)
public class AlibabaDruidProperties {

    private boolean enabled = true;

    private Filter filter = new Filter();

    /**
     * Return whether Alibaba Druid auto-configuration is enabled.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AlibabaDruidProperties props = new AlibabaDruidProperties();
     *   assertTrue(props.isEnabled()); // default is true
     * }</pre>
     *
     * @return {@code true} if Alibaba Druid is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether Alibaba Druid auto-configuration is enabled.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AlibabaDruidProperties props = new AlibabaDruidProperties();
     *   props.setEnabled(false);
     *   assertFalse(props.isEnabled());
     * }</pre>
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Return the nested {@link Filter} properties for Alibaba Druid.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AlibabaDruidProperties props = new AlibabaDruidProperties();
     *   Filter filterProps = props.getFilter();
     *   // filterProps.getClasses() returns [Filter.class] by default
     * }</pre>
     *
     * @return the {@link Filter} properties
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Set the nested {@link Filter} properties for Alibaba Druid.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AlibabaDruidProperties props = new AlibabaDruidProperties();
     *   Filter filterProps = new AlibabaDruidProperties.Filter();
     *   filterProps.setClasses(new Class[]{ LoggingStatementFilter.class });
     *   props.setFilter(filterProps);
     * }</pre>
     *
     * @param filter the {@link Filter} properties to set
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * The nested Properties class for Alibaba Druid {@link com.alibaba.druid.filter.Filter}
     */
    public static class Filter {

        private Class<? extends com.alibaba.druid.filter.Filter>[] classes = of(com.alibaba.druid.filter.Filter.class);

        /**
         * Return the filter bean classes to be added to each {@link DruidDataSource}.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   AlibabaDruidProperties.Filter filterProps = new AlibabaDruidProperties.Filter();
         *   Class<?>[] classes = filterProps.getClasses();
         *   // classes == [Filter.class] by default
         * }</pre>
         *
         * @return the filter classes
         */
        public Class<? extends com.alibaba.druid.filter.Filter>[] getClasses() {
            return classes;
        }

        /**
         * Set the filter bean classes to be added to each {@link DruidDataSource}.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   AlibabaDruidProperties.Filter filterProps = new AlibabaDruidProperties.Filter();
         *   filterProps.setClasses(new Class[]{ LoggingStatementFilter.class });
         * }</pre>
         *
         * @param classes the filter classes
         */
        public void setClasses(Class<? extends com.alibaba.druid.filter.Filter>[] classes) {
            this.classes = classes;
        }
    }
}
