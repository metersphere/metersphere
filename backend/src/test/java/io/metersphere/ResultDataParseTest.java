package io.metersphere;

import io.metersphere.report.base.Statistics;
import org.junit.Test;

import java.lang.reflect.Field;

public class ResultDataParseTest {

    String[] s = {"1","2","3","4","5","6","7","8","9","10","11","12","13"};

    public static <T> T setParam(Class<T> clazz, Object[] args)
            throws Exception {
        if (clazz == null || args == null) {
            throw new IllegalArgumentException();
        }
        T t = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null || fields.length > args.length) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            fields[i].set(t, args[i]);
        }
        return t;
    }

    @Test
    public void test() throws Exception {
        Statistics statistics = setParam(Statistics.class, s);
        System.out.println(statistics.toString());
    }


}
