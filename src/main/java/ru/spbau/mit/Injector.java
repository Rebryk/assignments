package ru.spbau.mit;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.lang.reflect.Constructor;
import java.util.*;


public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */
    private static HashMap<String, Boolean> classUsed;
    private static HashMap<String, Class<?>> classImpl;
    private static HashMap<String, Object> objectByClassName;

    private static Class<?> findClassImplInterface(Class<?> interfaceImpl, List<String> implementationClassNames) throws Exception {
        Class<?> type = null;
        for (String impl: implementationClassNames) {
            if (interfaceImpl.isAssignableFrom(Class.forName(impl))) {
                if (type == null) {
                    type = Class.forName(impl);
                } else {
                    throw new AmbiguousImplementationException();
                }
            }
        }
        return type;
    }

    private static Object initializeObject(String rootClassName, List<String> implementationClassNames) throws Exception {
        if (!classImpl.containsKey(rootClassName)) {
            throw new ImplementationNotFoundException();
        }

        if (classUsed.get(rootClassName)) {
            throw new InjectionCycleException();
        }
        classUsed.put(rootClassName, true);

        Class<?> rootClass = classImpl.get(rootClassName);
        Constructor<?> constructor = rootClass.getConstructors()[0];
        Class<?> types[] = constructor.getParameterTypes();

        Object[] parameters = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            if (types[i].isInterface()) {
                type = findClassImplInterface(types[i], implementationClassNames);
                if (type == null) {
                    throw new ImplementationNotFoundException();
                }
            }
            if (objectByClassName.containsKey(type.getCanonicalName())) {
                parameters[i] = objectByClassName.get(type.getCanonicalName());
            } else {
                parameters[i] = initializeObject(type.getCanonicalName(), implementationClassNames);
                objectByClassName.put(type.getCanonicalName(), parameters[i]);
            }
        }
        return constructor.newInstance(parameters);
    }

    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        objectByClassName = new HashMap<String, Object>();
        classUsed = new HashMap<String, Boolean>();
        classImpl = new HashMap<String, Class<?>>();

        classUsed.put(rootClassName, false);
        classImpl.put(rootClassName, Class.forName(rootClassName));
        for (String className: implementationClassNames) {
            classUsed.put(className, false);
            classImpl.put(className, Class.forName(className));
        }

        return initializeObject(rootClassName, implementationClassNames);
    }
}