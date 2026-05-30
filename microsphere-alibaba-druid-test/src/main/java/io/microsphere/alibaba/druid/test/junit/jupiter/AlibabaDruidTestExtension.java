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
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // AlibabaDruidTestExtension is automatically registered via @AlibabaDruidTest
 *   @AlibabaDruidTest
 *   class MyDruidTest {
 *
 *       @AlibabaDruidRuntime
 *       private DruidDataSource druidDataSource;
 *
 *       @Test
 *       void test() {
 *           assertNotNull(druidDataSource);
 *       }
 *   }
 * }</pre>
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

    /**
     * Inject candidate {@link AlibabaDruidRuntime}-annotated fields of the test instance
     * (or static fields if {@code testInstance} is {@code null}) with a {@link DruidDataSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called automatically by beforeAll (static fields) and postProcessTestInstance (instance fields)
     *   injectFields(context, testInstance);
     * }</pre>
     *
     * @param context      the current {@link ExtensionContext}
     * @param testInstance the test instance, or {@code null} for static field injection
     * @throws Exception if field injection fails
     */
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

    /**
     * Get or create a {@link DruidDataSource} stored in the extension context for the given scope.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSource ds = AlibabaDruidTestExtension.getDruidDataSource(context, false, false);
     *   assertNotNull(ds);
     * }</pre>
     *
     * @param context    the current {@link ExtensionContext}
     * @param forClass   {@code true} to use class-level store, {@code false} for method-level store
     * @param forRemoval {@code true} to remove the stored instance after retrieval
     * @return the {@link DruidDataSource}, never {@code null}
     * @throws SQLException if the data source cannot be initialized
     * @throws IOException  if properties cannot be loaded
     */
    static DruidDataSource getDruidDataSource(ExtensionContext context, boolean forClass, boolean forRemoval) throws SQLException, IOException {
        DruidDataSource druidDataSource = get(context, DruidDataSource.class, forClass, forRemoval);
        if (druidDataSource == null) {
            druidDataSource = buildDruidDataSource(context);
            store(context, druidDataSource, forClass);
        }
        return druidDataSource;
    }

    /**
     * Build a {@link DruidDataSource} from the {@link AlibabaDruidTest} annotation found on the
     * test class of the given {@link ExtensionContext}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSource ds = AlibabaDruidTestExtension.buildDruidDataSource(context);
     *   assertNotNull(ds);
     * }</pre>
     *
     * @param context the current {@link ExtensionContext}
     * @return a configured and initialized {@link DruidDataSource}
     * @throws SQLException if initialization fails
     * @throws IOException  if properties cannot be loaded
     */
    static DruidDataSource buildDruidDataSource(ExtensionContext context) throws SQLException, IOException {
        AlibabaDruidTest alibabaDruidTest = getAlibabaDruidTest(context);
        return buildDruidDataSource(alibabaDruidTest);
    }

    /**
     * Build a configured and initialized {@link DruidDataSource} from the given
     * {@link AlibabaDruidTest} annotation metadata.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @AlibabaDruidTest(propertiesResource = "META-INF/druid/h2.properties")
     *   class MyTest { }
     *
     *   AlibabaDruidTest annotation = MyTest.class.getAnnotation(AlibabaDruidTest.class);
     *   DruidDataSource ds = AlibabaDruidTestExtension.buildDruidDataSource(annotation);
     *   assertNotNull(ds);
     * }</pre>
     *
     * @param alibabaDruidTest the {@link AlibabaDruidTest} annotation
     * @return a configured and initialized {@link DruidDataSource}
     * @throws SQLException if initialization fails
     * @throws IOException  if properties cannot be loaded
     */
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

    /**
     * Find all candidate fields (annotated with {@link AlibabaDruidRuntime} and of a supported type)
     * in the test class associated with the given {@link ExtensionContext}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Set<Field> staticFields = AlibabaDruidTestExtension.findCandidateFields(context, true);
     *   Set<Field> instanceFields = AlibabaDruidTestExtension.findCandidateFields(context, false);
     * }</pre>
     *
     * @param context  the current {@link ExtensionContext}
     * @param isStatic {@code true} to find static fields, {@code false} for instance fields
     * @return a set of candidate {@link Field}s, never {@code null}
     */
    static Set<Field> findCandidateFields(ExtensionContext context, boolean isStatic) {
        Class<?> testClass = context.getRequiredTestClass();
        Predicate<? super Field> predicate = isStatic ? MemberUtils::isStatic : MemberUtils::isNonStatic;
        predicate = and(predicate, AlibabaDruidTestExtension::isCandidateField);
        return findAllDeclaredFields(testClass, predicate);
    }

    /**
     * Check whether the given {@link Parameter} is annotated with {@link AlibabaDruidRuntime}
     * and is of a supported Druid candidate type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Method method = MyTest.class.getMethod("test", DruidDataSource.class);
     *   Parameter param = method.getParameters()[0];
     *   boolean isCandidate = AlibabaDruidTestExtension.isCandidateParameter(param);
     * }</pre>
     *
     * @param parameter the {@link Parameter} to evaluate
     * @return {@code true} if the parameter can be resolved as a Druid runtime component
     */
    static boolean isCandidateParameter(Parameter parameter) {
        if (!isAlibabaDruidRuntime(parameter)) {
            return false;
        }
        return isCandidateType(parameter.getType());
    }

    /**
     * Check whether the given {@link Field} is annotated with {@link AlibabaDruidRuntime}
     * and is of a supported Druid candidate type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Field field = MyTest.class.getDeclaredField("druidDataSource");
     *   boolean isCandidate = AlibabaDruidTestExtension.isCandidateField(field);
     * }</pre>
     *
     * @param field the {@link Field} to evaluate
     * @return {@code true} if the field can be injected with a Druid runtime component
     */
    static boolean isCandidateField(Field field) {
        if (!isAlibabaDruidRuntime(field)) {
            return false;
        }
        return isCandidateType(field.getType());
    }

    /**
     * Check whether the given type is one of the supported Druid candidate types
     * (i.e., {@link DataSource} assignable or in the Alibaba Druid package).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   assertTrue(AlibabaDruidTestExtension.isCandidateType(DruidDataSource.class));
     *   assertFalse(AlibabaDruidTestExtension.isCandidateType(String.class));
     * }</pre>
     *
     * @param type the type to check
     * @return {@code true} if the type is a supported Druid component type
     */
    static boolean isCandidateType(Class<?> type) {
        return candidateTypes.contains(type);
    }

    /**
     * Check whether the given {@link AnnotatedElement} is annotated with {@link AlibabaDruidRuntime}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Field field = MyTest.class.getDeclaredField("druidDataSource");
     *   boolean hasAnnotation = AlibabaDruidTestExtension.isAlibabaDruidRuntime(field);
     * }</pre>
     *
     * @param annotatedElement the element to check
     * @return {@code true} if the element bears {@link AlibabaDruidRuntime}
     */
    static boolean isAlibabaDruidRuntime(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(AlibabaDruidRuntime.class);
    }

    /**
     * Check whether the given class is an Alibaba Druid class — either assignable from
     * {@link DataSource} or whose type name starts with the Alibaba Druid package prefix.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   assertTrue(AlibabaDruidTestExtension.isAlibabaDruidClass(DruidDataSource.class));
     *   assertFalse(AlibabaDruidTestExtension.isAlibabaDruidClass(String.class));
     * }</pre>
     *
     * @param klass the class to evaluate
     * @return {@code true} if the class is an Alibaba Druid component
     */
    static boolean isAlibabaDruidClass(Class<?> klass) {
        if (DataSource.class.isAssignableFrom(klass)) {
            return true;
        }
        return getTypeName(klass).startsWith(PACKAGE_NAME_PREFIX);
    }

    /**
     * Retrieve the {@link AlibabaDruidTest} annotation from the test class of the given
     * {@link ExtensionContext}, falling back to the enclosing class if necessary.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AlibabaDruidTest annotation = AlibabaDruidTestExtension.getAlibabaDruidTest(context);
     *   assertNotNull(annotation);
     * }</pre>
     *
     * @param context the current {@link ExtensionContext}
     * @return the {@link AlibabaDruidTest} annotation
     */
    static AlibabaDruidTest getAlibabaDruidTest(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        return getAlibabaDruidTest(testClass);
    }

    /**
     * Retrieve the {@link AlibabaDruidTest} annotation from the given test class,
     * falling back to the enclosing class if the annotation is not directly present.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AlibabaDruidTest annotation = AlibabaDruidTestExtension.getAlibabaDruidTest(MyTest.class);
     *   assertNotNull(annotation);
     * }</pre>
     *
     * @param testClass the test class
     * @return the {@link AlibabaDruidTest} annotation, or {@code null} if not found
     */
    static AlibabaDruidTest getAlibabaDruidTest(Class<?> testClass) {
        AlibabaDruidTest alibabaDruidTest = testClass.getAnnotation(AlibabaDruidTest.class);
        if (alibabaDruidTest == null) {
            testClass = testClass.getEnclosingClass();
            alibabaDruidTest = testClass.getAnnotation(AlibabaDruidTest.class);
        }
        return alibabaDruidTest;
    }

    /**
     * Close the given {@link DruidDataSource} if it is non-{@code null} and not already closed.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AlibabaDruidTestExtension.close(druidDataSource);
     *   // Safe to call even if druidDataSource is null or already closed
     *   AlibabaDruidTestExtension.close(null);
     * }</pre>
     *
     * @param druidDataSource the {@link DruidDataSource} to close, may be {@code null}
     */
    static void close(DruidDataSource druidDataSource) {
        if (druidDataSource != null && !druidDataSource.isClosed()) {
            druidDataSource.close();
        }
    }

    /**
     * Store the given component in the appropriate extension {@link Store}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AlibabaDruidTestExtension.store(context, druidDataSource, false); // method-level store
     *   AlibabaDruidTestExtension.store(context, druidDataSource, true);  // class-level store
     * }</pre>
     *
     * @param context   the current {@link ExtensionContext}
     * @param component the component to store
     * @param forClass  {@code true} to store in class-level store, {@code false} for method-level
     */
    static void store(ExtensionContext context, Object component, boolean forClass) {
        Store store = getStore(context, forClass);
        Object key = buildKey(context, component.getClass());
        store.put(key, component);
    }

    /**
     * Build a store key combining the test method signature (or test class name) and component class name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Object key = AlibabaDruidTestExtension.buildKey(context, DruidDataSource.class);
     *   // key is a "method-signature:com.alibaba.druid.pool.DruidDataSource" string
     * }</pre>
     *
     * @param context        the current {@link ExtensionContext}
     * @param componentClass the class of the component to key by
     * @return a non-null string key
     */
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

    /**
     * Retrieve a component from the appropriate extension {@link Store}, optionally removing it.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DruidDataSource ds = AlibabaDruidTestExtension.get(context, DruidDataSource.class, false, false);
     *   // retrieve and remove from class-level store:
     *   DruidDataSource removed = AlibabaDruidTestExtension.get(context, DruidDataSource.class, true, true);
     * }</pre>
     *
     * @param context       the current {@link ExtensionContext}
     * @param componentType the expected type
     * @param forClass      {@code true} to use class-level store, {@code false} for method-level
     * @param forRemoval    {@code true} to remove the entry after retrieval
     * @param <T>           the component type
     * @return the stored component, or {@code null} if not found
     */
    static <T> T get(ExtensionContext context, Class<T> componentType, boolean forClass, boolean forRemoval) {
        Object key = buildKey(context, componentType);
        Store store = getStore(context, forClass);
        return forRemoval ? store.remove(key, componentType) : store.get(key, componentType);
    }

    /**
     * Retrieve the appropriate extension {@link Store} for class-level or method-level scope.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Store methodStore = AlibabaDruidTestExtension.getStore(context, false);
     *   Store classStore  = AlibabaDruidTestExtension.getStore(context, true);
     * }</pre>
     *
     * @param context  the current {@link ExtensionContext}
     * @param forClass {@code true} for class-level store, {@code false} for method-level
     * @return the corresponding {@link Store}
     */
    static Store getStore(ExtensionContext context, boolean forClass) {
        Namespace namespace = forClass ? CLASS_NAMESPACE : METHOD_NAMESPACE;
        return context.getStore(EXTENSION_CONTEXT, namespace);
    }
}
