package io.metersphere.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DistinctKeyUtil {

    /**
     * 按照某个字段去重
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> List<T> distinctByKey(List<T> list, Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> map = new HashMap<>();
        return list.stream().filter(t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null)
                .collect(Collectors.toList());
    }
}
