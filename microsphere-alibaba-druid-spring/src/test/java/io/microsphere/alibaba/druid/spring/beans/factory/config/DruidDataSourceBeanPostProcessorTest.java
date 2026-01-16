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

import io.microsphere.alibaba.druid.filter.LoggingStatementFilter;
import io.microsphere.alibaba.druid.test.spring.AbstractDruidSpringTest;
import io.microsphere.alibaba.druid.test.spring.DruidDataSourceTestConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * {@link DruidDataSourceBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DruidDataSourceBeanPostProcessor
 * @since 1.0.0
 */
@ExtendWith(value = SpringExtension.class)
@ContextConfiguration(classes = {
        LoggingStatementFilter.class,
        DruidDataSourceTestConfiguration.class,
        DruidDataSourceBeanPostProcessor.class,
        DruidDataSourceBeanPostProcessorTest.class,
})
public class DruidDataSourceBeanPostProcessorTest extends AbstractDruidSpringTest {

}
