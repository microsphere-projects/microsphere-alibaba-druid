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
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Typically registered via @EnableAlibabaDruid; can also be registered manually:
 *   @Bean
 *   public DruidDataSourceBeanPostProcessor druidDataSourceBeanPostProcessor() {
 *       return new DruidDataSourceBeanPostProcessor(new Class[]{ LoggingStatementFilter.class });
 *   }
 *   // All Filter beans of the given classes will be added to every DruidDataSource bean
 *   // before initialization.
 * }</pre>
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

    /**
     * Create a {@link DruidDataSourceBeanPostProcessor} that adds all {@link Filter} beans
     * (regardless of subtype) to every {@link DruidDataSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourceBeanPostProcessor processor = new DruidDataSourceBeanPostProcessor();
     *   // Equivalent to: new DruidDataSourceBeanPostProcessor(new Class[]{ Filter.class })
     * }</pre>
     */
    public DruidDataSourceBeanPostProcessor() {
        this(of(Filter.class));
    }

    /**
     * Create a {@link DruidDataSourceBeanPostProcessor} with the specified filter bean classes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSourceBeanPostProcessor processor =
     *       new DruidDataSourceBeanPostProcessor(new Class[]{ LoggingStatementFilter.class });
     *   // Only LoggingStatementFilter beans will be added to DruidDataSource beans
     * }</pre>
     *
     * @param filterBeanClasses the filter bean classes whose Spring beans will be added to each
     *                          {@link DruidDataSource} before initialization
     */
    public DruidDataSourceBeanPostProcessor(Class<? extends Filter>[] filterBeanClasses) {
        this.filterBeanClasses = filterBeanClasses;
        if (logger.isTraceEnabled()) {
            logger.trace("The classes of Filter Bean to be added : {}", arrayToString(filterBeanClasses));
        }
    }

    /**
     * Invoked before the {@link DruidDataSource} bean is initialized, adding all configured
     * {@link Filter} beans to the data source's proxy filter list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called automatically by Spring for each DruidDataSource bean before initialization
     *   // Adds all Filter beans of the configured classes to druidDataSource.getProxyFilters()
     * }</pre>
     *
     * @param druidDataSource the {@link DruidDataSource} bean being initialized
     * @param beanName        the name of the bean
     * @throws BeansException if filter initialization fails
     */
    @Override
    protected void processBeforeInitialization(DruidDataSource druidDataSource, String beanName) throws BeansException {
        initializeFilterBeans(druidDataSource);
    }

    /**
     * Collect all {@link Filter} beans matching the configured filter bean classes from the
     * {@link BeanFactory} and add them (sorted) to the {@link DruidDataSource}'s proxy filter list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called automatically by processBeforeInitialization
     *   initializeFilterBeans(druidDataSource);
     *   // All sorted Filter beans are added to druidDataSource.getProxyFilters()
     * }</pre>
     *
     * @param druidDataSource the {@link DruidDataSource} to add filters to
     */
    protected void initializeFilterBeans(DruidDataSource druidDataSource) {
        List<Filter> filterBeans = new LinkedList<>();
        for (Class<? extends Filter> filterBeanClass : filterBeanClasses) {
            filterBeans.addAll(getSortedBeans(this.beanFactory, filterBeanClass));
        }
        sort(filterBeans);
        if (logger.isTraceEnabled()) {
            logger.trace("The {} Filter Beans[{}] will be added into DruidDataSource[{}]", filterBeans.size(), filterBeans, druidDataSource);
        }
        druidDataSource.getProxyFilters().addAll(filterBeans);
    }

    /**
     * Set the {@link BeanFactory} used to look up {@link Filter} beans.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Injected automatically by Spring when the processor is registered as a bean
     *   processor.setBeanFactory(applicationContext);
     * }</pre>
     *
     * @param beanFactory the owning {@link BeanFactory}
     * @throws BeansException if setting the factory fails
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
