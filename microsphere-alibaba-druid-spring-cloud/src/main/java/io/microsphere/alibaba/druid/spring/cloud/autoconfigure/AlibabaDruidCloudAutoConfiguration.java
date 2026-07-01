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

import io.microsphere.alibaba.druid.spring.boot.AlibabaDruidProperties;
import io.microsphere.alibaba.druid.spring.boot.condition.ConditionalOnAlibabaDruidAvailable;

/**
 * The Spring Cloud Auto-Configuration for Alibaba Druid
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   # application.properties
 *   microsphere.alibaba.druid.enabled=true
 *
 *   # When Spring Cloud is on the classpath and Alibaba Druid is available,
 *   # this auto-configuration registers Druid features with Spring Cloud Actuator.
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AlibabaDruidProperties
 * @since 1.0.0
 */
@ConditionalOnAlibabaDruidAvailable
public class AlibabaDruidCloudAutoConfiguration {
}
