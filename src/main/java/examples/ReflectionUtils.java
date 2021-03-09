package examples;

import java.lang.reflect.Method;

public class ReflectionUtils {

    public static Method getMethod(Class<?> cls, String implClassName, Class<?>... prams) throws Exception {
        Method method = cls.getDeclaredMethod(implClassName, prams);
        method.setAccessible(true);
        return method;
    }

}
