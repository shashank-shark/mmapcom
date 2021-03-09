package examples;

import sun.nio.ch.FileChannelImpl;

import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

public class ReflectionTest {

    public static void main(String[] args) throws Exception {
        Method m = getMethod(ReflectionExampleTarget.class, "mapX400", long.class, long.class);
        System.out.println(m.getReturnType());

    }

    private static Method getMethod(Class<?> c, String implClass, Class<?>... params) throws Exception {
        Method m = c.getDeclaredMethod(implClass, params);
        m.setAccessible(true);
        return m;
    }
}
