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

package io.microsphere.alibaba.druid.test.junit.jupiter;

import com.alibaba.druid.pool.DruidDataSource;
import io.microsphere.annotation.Nullable;
import io.microsphere.reflect.MemberUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.PACKAGE_NAME_PREFIX;
import static io.microsphere.alibaba.druid.test.AlibabaDruidTestUtils.loadProperties;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.FieldUtils.setFieldValue;
import static io.microsphere.reflect.MethodUtils.getSignature;
import static io.microsphere.util.ClassUtils.findAllClasses;
import static io.microsphere.util.ClassUtils.getTypeName;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.jupiter.api.extension.ExtensionContext.StoreScope.EXTENSION_CONTEXT;

/**
 * The JUnit Jupiter Test {@link Extension} of Alibaba Druid Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AlibabaDruidTest
 * @see AlibabaDruidRuntime
 * @see DruidDataSource
 * @since 1.0.0
 */
class AlibabaDruidTestExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
        AfterEachCallback, ParameterResolver {

    private static final Namespace METHOD_NAMESPACE = create(AlibabaDruidTestExtension.class, "METHOD");

    private static final Namespace CLASS_NAMESPACE = create(AlibabaDruidTestExtension.class, "CLASS");

    static List<Class<?>> candidateTypes = findAllClasses(DruidDataSource.class, AlibabaDruidTestExtension::isAlibabaDruidClass);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        injectFields(context, null);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        DruidDataSource druidDataSource = get(context, DruidDataSource.class, true, true);
        close(druidDataSource);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        injectFields(context, testInstance);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        DruidDataSource druidDataSource = get(context, DruidDataSource.class, false, true);
        close(druidDataSource);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Parameter parameter = parameterContext.getParameter();
        return isCandidateParameter(parameter);
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return execute(() -> getDruidDataSource(extensionContext, false, false));
    }

    void injectFields(ExtensionContext context, @Nullable Object testInstance) throws Exception {
        boolean isStatic = testInstance == null;
        Set<Field> fields = findCandidateFields(context, isStatic);
        for (Field field : fields) {
            injectField(field, context, testInstance);
        }
    }

    private void injectField(Field field, ExtensionContext context, @Nullable Object testInstance) throws Exception {
        boolean forClass = testInstance == null;
        DruidDataSource druidDataSource = getDruidDataSource(context, forClass, false);
        setFieldValue(testInstance, field, druidDataSource);
    }

    static DruidDataSource getDruidDataSource(ExtensionContext context, boolean forClass, boolean forRemoval) throws SQLException, IOException {
        DruidDataSource druidDataSource = get(context, DruidDataSource.class, forClass, forRemoval);
        if (druidDataSource == null) {
            druidDataSource = buildDruidDataSource(context);
            store(context, druidDataSource, forClass);
        }
        return druidDataSource;
    }

    static DruidDataSource buildDruidDataSource(ExtensionContext context) throws SQLException, IOException {
        AlibabaDruidTest alibabaDruidTest = getAlibabaDruidTest(context);
        return buildDruidDataSource(alibabaDruidTest);
    }

    static DruidDataSource buildDruidDataSource(AlibabaDruidTest alibabaDruidTest) throws SQLException, IOException {
        String propertiesResource = alibabaDruidTest.propertiesResource();
        Properties properties = loadProperties(propertiesResource);
        Properties overridenProperties = loadProperties(alibabaDruidTest.properties());
        properties.putAll(overridenProperties);
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.configFromProperties(properties);
        druidDataSource.init();
        return druidDataSource;
    }

    static Set<Field> findCandidateFields(ExtensionContext context, boolean isStatic) {
        Class<?> testClass = context.getRequiredTestClass();
        Predicate<? super Field> predicate = isStatic ? MemberUtils::isStatic : MemberUtils::isNonStatic;
        predicate = and(predicate, AlibabaDruidTestExtension::isCandidateField);
        return findAllDeclaredFields(testClass, predicate);
    }

    static boolean isCandidateParameter(Parameter parameter) {
        if (!isAlibabaDruidRuntime(parameter)) {
            return false;
        }
        return isCandidateType(parameter.getType());
    }

    static boolean isCandidateField(Field field) {
        if (!isAlibabaDruidRuntime(field)) {
            return false;
        }
        return isCandidateType(field.getType());
    }

    static boolean isCandidateType(Class<?> type) {
        return candidateTypes.contains(type);
    }

    static boolean isAlibabaDruidRuntime(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(AlibabaDruidRuntime.class);
    }

    static boolean isAlibabaDruidClass(Class<?> klass) {
        if (DataSource.class.isAssignableFrom(klass)) {
            return true;
        }
        return getTypeName(klass).startsWith(PACKAGE_NAME_PREFIX);
    }

    static AlibabaDruidTest getAlibabaDruidTest(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        return getAlibabaDruidTest(testClass);
    }

    static AlibabaDruidTest getAlibabaDruidTest(Class<?> testClass) {
        AlibabaDruidTest alibabaDruidTest = testClass.getAnnotation(AlibabaDruidTest.class);
        if (alibabaDruidTest == null) {
            testClass = testClass.getEnclosingClass();
            alibabaDruidTest = testClass.getAnnotation(AlibabaDruidTest.class);
        }
        return alibabaDruidTest;
    }

    static void close(DruidDataSource druidDataSource) {
        if (druidDataSource != null && !druidDataSource.isClosed()) {
            druidDataSource.close();
        }
    }

    static void store(ExtensionContext context, Object component, boolean forClass) {
        Store store = getStore(context, forClass);
        Object key = buildKey(context, component.getClass());
        store.put(key, component);
    }

    static Object buildKey(ExtensionContext context, Class<?> componentClass) {
        StringJoiner keyBuilder = new StringJoiner(":");
        Method method = context.getTestMethod().orElse(null);
        if (method == null) {
            Class<?> testClass = context.getRequiredTestClass();
            keyBuilder.add(testClass.getName());
        } else {
            keyBuilder.add(getSignature(method));
        }
        keyBuilder.add(componentClass.getName());
        return keyBuilder.toString();
    }

    static <T> T get(ExtensionContext context, Class<T> componentType, boolean forClass, boolean forRemoval) {
        Object key = buildKey(context, componentType);
        Store store = getStore(context, forClass);
        return forRemoval ? store.remove(key, componentType) : store.get(key, componentType);
    }

    static Store getStore(ExtensionContext context, boolean forClass) {
        Namespace namespace = forClass ? CLASS_NAMESPACE : METHOD_NAMESPACE;
        return context.getStore(EXTENSION_CONTEXT, namespace);
    }
}