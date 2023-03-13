package com.inversion.ioc;

import com.inversion.annotation.Bean;
import com.inversion.ioc.util.ReflectionUtil;
import org.burningwave.core.assembler.ComponentContainer;
import org.burningwave.core.classes.ClassCriteria;
import org.burningwave.core.classes.SearchConfig;

import javax.management.RuntimeErrorException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Injector {
    private Map<Class<?>, Class<?>> dependencies;
    private Map<Class<?>, Object> applicationContext;
    private static Injector injector;

    private Injector() {
        super();
        dependencies = new HashMap<>();
        applicationContext = new HashMap<>();
    }

    public static void startup(Class<?> mainClass) throws InstantiationException, IllegalAccessException {
        try {
            synchronized (Injector.class) {
                if (injector == null) {
                    injector = new Injector();
                    injector.init(mainClass);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T getBean(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        try {
            return injector.getBeanInstance(clazz);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void init(Class<?> mainClass) throws InstantiationException, IllegalAccessException {
        final var classes = getClasses(mainClass.getPackage().getName(), true);
        final var componentContainer = ComponentContainer.getInstance();
        final var classHunter = componentContainer.getClassHunter();
        final var packagePath = mainClass.getPackage().getName().replace(".", "/");

        try (final var searchResult = classHunter.findBy(
                SearchConfig
                        .forResources(packagePath)
                        .by(ClassCriteria.create().allThoseThatMatch(cls -> cls.getAnnotation(Bean.class) != null)))
        ) {
            final var types = searchResult.getClasses();

            for (final var implementation: types) {
                final var interfaces = implementation.getInterfaces();

                if (interfaces.length == 0) {
                    dependencies.put(implementation, implementation);
                } else {
                    for (final var i : interfaces) {
                        dependencies.put(implementation, i);
                    }
                }
            }

            for (final var clazz : classes) {
                if (clazz.isAnnotationPresent(Bean.class)) {
                    final var classInstance = clazz.newInstance();
                    applicationContext.put(clazz, classInstance);
                    ReflectionUtil.inject(this, clazz, classInstance);
                }
            }
        }
    }

    public Class<?>[] getClasses(String packageName, boolean recursive) {
        final var componentContainer = ComponentContainer.getInstance();
        final var classHunter = componentContainer.getClassHunter();
        final var packagePath = packageName.replace(".", "/");
        final var searchConfig = SearchConfig.forResources(packagePath);

        if (!recursive) {
            searchConfig.findInChildren();
        }

        try (final var searchResult = classHunter.findBy(searchConfig)) {
            final var classes = searchResult.getClasses();

            return classes.toArray(new Class[0]);
        }
    }

    public <T> Object getBeanInstance(Class<T> interfaceClass, String fieldName, String qualifier) throws InstantiationException, IllegalAccessException {
        final var implementationClass = getImplimentationClass(interfaceClass, fieldName, qualifier);

        if (applicationContext.containsKey(implementationClass)) {
            return applicationContext.get(implementationClass);
        }

        synchronized (applicationContext) {
            Object service = implementationClass.newInstance();
            applicationContext.put(implementationClass, service);
            return service;
        }
    }

    private Class<?> getImplimentationClass(Class<?> interfaceClass, final String fieldName, final String qualifier) {
        final var implementationClasses = dependencies.entrySet().stream()
                .filter(entry -> entry.getValue() == interfaceClass).collect(Collectors.toSet());
        var errorMessage = "";
        if (implementationClasses.size() == 0) {
            errorMessage = "error for " + interfaceClass.getName();
        } else if (implementationClasses.size() == 1) {
            final var optional = implementationClasses.stream().findFirst();
            return optional.get().getKey();
        } else {
            final String findBy = (qualifier == null || qualifier.trim().length() == 0) ? fieldName : qualifier;
            final var optional = implementationClasses.stream()
                    .filter(entry -> entry.getKey().getSimpleName().equalsIgnoreCase(findBy)).findAny();
            if (optional.isPresent()) {
                return optional.get().getKey();
            } else {
                errorMessage = "error";
            }
        }
        throw new RuntimeErrorException(new Error(errorMessage));
    }

    private <T> T getBeanInstance(final Class<T> i) throws InstantiationException, IllegalAccessException {
        return (T) getBeanInstance(i, null, null);
    }
}
