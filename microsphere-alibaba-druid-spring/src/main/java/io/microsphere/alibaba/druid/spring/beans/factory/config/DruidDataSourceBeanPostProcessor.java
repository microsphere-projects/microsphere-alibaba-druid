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
package io.microsphere.alibaba.druid.spring.beans.factory.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.config.GenericBeanPostProcessorAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.util.LinkedList;
import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.of;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * The {@link GenericBeanPostProcessorAdapter} for {@link DruidDataSource}
 * {@link #processBeforeInitialization(DruidDataSource, String) processes before initialization} :
 * <ul>
 *     <li>{@link #initializeFilterBeans(DruidDataSource) Initialize} {@link Filter filter} beans if specified</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DruidDataSource
 * @since 1.0.0
 */
public class DruidDataSourceBeanPostProcessor extends GenericBeanPostProcessorAdapter<DruidDataSource> implements BeanFactoryAware {

    private static final Logger logger = getLogger(DruidDataSourceBeanPostProcessor.class);

    /**
     * The Spring Bean Name of {@link DruidDataSourceBeanPostProcessor}
     */
    public static final String BEAN_NAME = "druidDataSourceBeanPostProcessor";

    private final Class<? extends Filter>[] filterBeanClasses;

    private BeanFactory beanFactory;

    public DruidDataSourceBeanPostProcessor() {
        this(of(Filter.class));
    }

    public DruidDataSourceBeanPostProcessor(Class<? extends Filter>[] filterBeanClasses) {
        this.filterBeanClasses = filterBeanClasses;
        logger.trace("The classes of Filter Bean to be added : {}", arrayToString(filterBeanClasses));
    }

    @Override
    protected void processBeforeInitialization(DruidDataSource druidDataSource, String beanName) throws BeansException {
        initializeFilterBeans(druidDataSource);
    }

    protected void initializeFilterBeans(DruidDataSource druidDataSource) {
        List<Filter> filterBeans = new LinkedList<>();
        for (Class<? extends Filter> filterBeanClass : filterBeanClasses) {
            filterBeans.addAll(getSortedBeans(this.beanFactory, filterBeanClass));
        }
        sort(filterBeans);
        logger.trace("The {} Filter Beans[{}] will be added into DruidDataSource[{}]", filterBeans.size(), filterBeans, druidDataSource);
        druidDataSource.getProxyFilters().addAll(filterBeans);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
