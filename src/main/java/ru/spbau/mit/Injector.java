package ru.spbau.mit;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */
    private static HashMap<String, Boolean> classUsed;
    private static HashMap<String, Boolean> classEnabled;
    private static HashMap<String, Class<?>> classImpl;

    private static Class<?> findClassImplInterface(Class<?> interfaceImpl) throws Exception {
        Class<?> type = null;
        for (HashMap.Entry<String, Class<?>> entry : classImpl.entrySet()) {
            if (interfaceImpl.isAssignableFrom(entry.getValue())) {
                if (type == null) {
                    type = entry.getValue();
                } else {
                    throw new AmbiguousImplementationException();
                }
                break;
            }

            /*Class<?>[] implClasses = entry.getValue().getClasses();
            for (Class<?> implClass: implClasses) {
                if (implClass.getCanonicalName().equals(interfaceName)) {
                    if (type == null) {
                        type = implClass;
                    } else {
                        throw new AmbiguousImplementationException();
                    }
                    break;
                }
            }*/
        }
        return type;
    }

    private static Object initialize(String rootClassName) throws Exception {
        if (classUsed.get(rootClassName)) {
            throw new InjectionCycleException();
        }

        if (!classEnabled.get(rootClassName)) {
            throw new ImplementationNotFoundException();
        }

        Class<?> rootClass = Class.forName(rootClassName);
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
            parameters[i] = initialize(type.getCanonicalName());
        }
        Object object = constructor.newInstance(parameters);
        classUsed.put(rootClassName, true);
        return object;
    }

    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        classUsed = new HashMap<String, Boolean>();
        classEnabled = new HashMap<String, Boolean>();
        classImpl = new HashMap<String, Class<?>>();

        classUsed.put(rootClassName, false);
        classEnabled.put(rootClassName, true);
        classImpl.put(rootClassName, Class.forName(rootClassName));
        for (String className: implementationClassNames) {
            classUsed.put(className, false);
            classEnabled.put(className, true);
            classImpl.put(className, Class.forName(className));
        }

        return initialize(rootClassName);
    }
}