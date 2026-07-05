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
package io.microsphere.alibaba.druid.constants;

import io.microsphere.annotation.ConfigurationProperty;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.constants.SymbolConstants.DOT;

/**
 * The constants of Alibaba Druid Properties
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Access the property name prefix for Alibaba Druid
 *   String prefix = PropertyConstants.ALIBABA_DRUID_PROPERTY_NAME_PREFIX;
 *   // prefix == "microsphere.alibaba.druid"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see io.microsphere.constants.PropertyConstants
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Alibaba Druid : "microsphere.alibaba.druid."
     */
    String ALIBABA_DRUID_PROPERTY_NAME_PREFIX = MICROSPHERE_PROPERTY_NAME_PREFIX + "alibaba.druid" + DOT;

    /**
     * The default property value of Alibaba Druid enabled : "true"
     */
    String DEFAULT_ALIBABA_DRUID_ENABLED_PROPERTY_VALUE = "true";

    /**
     * The property name of Alibaba Druid enabled : "microsphere.alibaba.druid.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_ALIBABA_DRUID_ENABLED_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    String ALIBABA_DRUID_ENABLED_PROPERTY_NAME = ALIBABA_DRUID_PROPERTY_NAME_PREFIX + ENABLED_PROPERTY_NAME;
}