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

import io.microsphere.alibaba.druid.test.AbstractAlibabaDruidTest;
import io.microsphere.alibaba.druid.test.spring.AbstractDruidSpringTest;
import io.microsphere.lang.function.ThrowableConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static java.sql.Statement.NO_GENERATED_KEYS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Abstract Druid Spring Boot Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AlibabaDruidProperties
 * @since 1.0.0
 */
public class AbstractDruidSpringBootTest extends AbstractDruidSpringTest {

    @Autowired
    private AlibabaDruidProperties alibabaDruidProperties;
}
