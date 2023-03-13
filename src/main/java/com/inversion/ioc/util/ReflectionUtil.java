package com.inversion.ioc.util;

import com.inversion.annotation.Inject;
import com.inversion.annotation.Qualifier;
import com.inversion.ioc.Injector;
import org.burningwave.core.classes.FieldCriteria;
import org.burningwave.core.classes.Fields;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReflectionUtil {

    public static void inject(Injector injector, Class<?> clazz, Object classInstance) throws InstantiationException, IllegalAccessException {
        Set<Field> fields = findFields(clazz);
        for (Field field : fields) {
            String qualifier = field.isAnnotationPresent(Inject.class)
                    ? field.getAnnotation(Qualifier.class).value()
                    : null;
            Object fieldInstance = injector.getBeanInstance(field.getType(), field.getName(), qualifier);
            field.set(classInstance, fieldInstance);
            inject(injector, fieldInstance.getClass(), fieldInstance);
        }
    }

    private static Set<Field> findFields(Class<?> clazz) {
        Set<Field> set = new HashSet<>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    set.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return set;
    }
}
