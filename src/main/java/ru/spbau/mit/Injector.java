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

    private static Class<?> findClassImplInterface(Class<?> interfaceImpl) throws Exception {
        Class<?> type = null;
        for (Map.Entry<String, Class<?>> entry: classImpl.entrySet()) {
            if (interfaceImpl.isAssignableFrom(entry.getValue())) {
                if (type == null) {
                    type = entry.getValue();
                } else {
                    throw new AmbiguousImplementationException();
                }
            }
        }
        return type;
    }

    private static Object initialize(String rootClassName) throws Exception {
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
                type = findClassImplInterface(types[i]);
                if (type == null) {
                    throw new ImplementationNotFoundException();
                }
            }
            if (objectByClassName.containsKey(type.getCanonicalName())) {
                parameters[i] = objectByClassName.get(type.getCanonicalName());
            } else {
                parameters[i] = initialize(type.getCanonicalName());
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

        return initialize(rootClassName);
    }
}