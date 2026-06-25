package com.uam.psychoform.dto;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.*;

public final class EntityView {
    private EntityView() {
    }

    public static Object of(Object value) {
        if (value == null || scalar(value)) {
            return value;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(EntityView::of).toList();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Method method : value.getClass().getMethods()) {
            if (method.getParameterCount() != 0 || method.getName().equals("getClass")) {
                continue;
            }
            String property = propertyName(method.getName());
            if (property == null) {
                continue;
            }
            try {
                Object propertyValue = method.invoke(value);
                if (propertyValue == null || scalar(propertyValue)) {
                    result.put(property, propertyValue);
                } else if (hasId(propertyValue)) {
                    result.put(property + "Id", propertyValue.getClass().getMethod("getId").invoke(propertyValue));
                }
            } catch (ReflectiveOperationException ignored) {
                // Best-effort API projection. Lazy relations are intentionally skipped.
            }
        }
        return result;
    }

    private static boolean hasId(Object value) {
        try {
            value.getClass().getMethod("getId");
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    private static String propertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        return null;
    }

    private static boolean scalar(Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof UUID
                || value instanceof Enum<?> || value instanceof Temporal || value instanceof BigDecimal;
    }
}

