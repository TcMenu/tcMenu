package com.thecoderscorner.embedcontrol.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * BaseMenuConfig is a base class for configuring menu settings and properties. It holds both the core configuration
 * and also extra configuration added by plugins, it is assumed that the plugin based configuration does not have any
 * dependencies outside of those that are declared in its parameters.
 */
public class BaseMenuConfig {
    protected final Map<Class<?>, Object> componentMap = new ConcurrentHashMap<>();
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    protected final String environment;
    protected final Properties resolvedProperties;
    protected final String baseName;

    /**
     * this constructor ensures that the environment and properties are initialised. To configure the environment from
     * system properties set `env` to `null` and set system property `tc.env` to the environment name.
     * @param env the environment or null to evaluate from system properties.
     */
    public BaseMenuConfig(String baseAppName, String env) {
        environment = (env != null) ? env : System.getProperty("tc.env", "dev");
        logger.log(System.Logger.Level.INFO, "Starting app in environment " + environment);
        baseName = baseAppName != null ? baseAppName : "application";
        resolvedProperties = resolveProperties(environment);
    }

    /**
     * Resolves properties based on the specified environment.
     *
     * @param environment the environment for which properties need to be resolved
     * @return the resolved properties as a Properties object
     */
    protected Properties resolveProperties(String environment) {
        Properties p = new Properties();
        try(var envProps = getClass().getResourceAsStream(String.format("/%s_%s.properties", baseName, environment));
            var globalProps = getClass().getResourceAsStream("/" + baseName + ".properties")) {
            if(globalProps != null) {
                logger.log(System.Logger.Level.INFO, "Reading global properties from " + globalProps);
                p.load(globalProps);
            }

            if(envProps != null) {
                logger.log(System.Logger.Level.INFO, "Reading env properties from " + envProps);
                p.load(envProps);
            }
            logger.log(System.Logger.Level.INFO, "App Properties read finished");
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed to read app property files", ex);
        }
        return p;
    }

    /**
     * Scans for components annotated with {@code TcComponent} in the current class and stores them in the component map.
     * If an annotated method fails to invoke, an error message will be logged. Further, you can add parameters that will
     * be resolved, but this code cannot resolve circular dependencies, it will scan as long as at one component is built
     * each time around the loop.
     */
    protected void scanForComponents() {
        var toResolve = Arrays.stream(getClass().getDeclaredMethods()).filter(m -> m.isAnnotationPresent(TcComponent.class))
                .collect(Collectors.toCollection(ArrayList::new));
        while(!toResolve.isEmpty()) {
            var resolvedThisTurn = new ArrayList<Method>();
            for (var m : toResolve) {
                m.setAccessible(true);
                try {
                    if (m.getParameters().length == 0) {
                        logger.log(System.Logger.Level.DEBUG, "Found " + m.getName() + " to fulfill " + m.getReturnType().getSimpleName());
                        componentMap.put(m.getReturnType(), m.invoke(this));
                        resolvedThisTurn.add(m);
                    } else {
                        var params = resolveParametersOrFail(m);
                        if(params == null) continue;
                        logger.log(System.Logger.Level.DEBUG, "Found " + m.getName() + " parameterized to fulfill " + m.getReturnType().getSimpleName());
                        componentMap.put(m.getReturnType(), m.invoke(this, params.toArray()));
                        resolvedThisTurn.add(m);
                    }
                } catch (Exception e) {
                    logger.log(System.Logger.Level.ERROR, "Skipping bean " + m.getName() + " because it failed", e);
                }
            }
            if(resolvedThisTurn.isEmpty() && !toResolve.isEmpty()) {
                throw new UnsupportedOperationException("Probably circular dependency in config, cannot resolve");
            } else {
                toResolve.removeAll(resolvedThisTurn);
            }
        }
    }

    private ArrayList<Object> resolveParametersOrFail(Method m) {
        try {
            var params = new ArrayList<>();
            for (Parameter param : m.getParameters()) {
                Class<?> paramType = param.getType();
                Object resolvedParam = componentMap.get(paramType);
                if (resolvedParam == null) {
                    resolvedParam = componentMap.entrySet().stream()
                            .filter(e -> paramType.isAssignableFrom(e.getKey()))
                            .findFirst()
                            .orElseThrow()
                            .getValue();
                }
                params.add(resolvedParam);
            }
            return params;
        } catch (Exception ex) {
            logger.log(System.Logger.Level.DEBUG, "Can't resolve " + m.getName() + " yet, unresolved dependencies");
            return null;
        }
    }

    /**
     * Retrieves the current environment.
     *
     * @return the current environment as a String
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Retrieves the resolved properties.
     *
     * @return the resolved properties as a Properties object
     */
    public Properties getResolvedProperties() {
        return resolvedProperties;
    }

    /**
     * Gets the value of the specified property as an integer. If the property exists in the resolved properties,
     * it will be parsed as an integer and returned. If the property does not exist, the default value will be returned.
     *
     * @param propName the name of the property to retrieve the value from
     * @param def the default value to return if the property does not exist
     * @return the value of the property as an integer, or the default value if the property does not exist
     */
    protected int propAsIntWithDefault(String propName, int def) {
        if(resolvedProperties.containsKey(propName)) {
            return Integer.parseInt(resolvedProperties.getProperty(propName));
        }
        return def;
    }

    /**
     * Retrieves the value of the specified property and throws an exception if missing.
     *
     * @param propName the name of the property to retrieve
     * @return the value of the property
     * @throws IllegalArgumentException if the property is missing in the configuration
     */
    protected String mandatoryStringProp(String propName) {
        if(!resolvedProperties.containsKey(propName)) {
            throw new IllegalArgumentException("Missing property in configuration " + propName);
        }
        return resolvedProperties.getProperty(propName);
    }

    /**
     * Retrieves an instance of the specified class from the component map. Fastest lookup is by direct class type.
     * However, if the class is not found in the map, it searches for a compatible class that would fulfill the
     * interface provided. If no compatible class is found, an exception is thrown.
     *
     * @param clazz the class to retrieve an instance of
     * @param <T>   the generic type of the class, the function is T <= class T
     * @return an instance of the specified class
     * @throws NoSuchElementException if no compatible class is found in the component map
     */
    public <T> T getBean(Class<T> clazz) {
        var cmp = componentMap.get(clazz);
        if(cmp == null) {
            cmp = componentMap.entrySet().stream().filter(e -> clazz.isAssignableFrom(e.getKey())).findFirst()
                    .orElseThrow().getValue();
        }
        return (T) cmp;
    }

    /**
     * Adds a bean to the component map and returns it.
     *
     * @param beanToAdd the bean to be added to the component map
     * @param <T>       the type of the bean
     * @return the added bean
     */
    public <T> T asBean(T beanToAdd) {
        componentMap.put(beanToAdd.getClass(), beanToAdd);
        return beanToAdd;
    }

}
