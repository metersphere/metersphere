package io.metersphere.sdk.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SubListUtils {

    public static int DEFAULT_BATCH_SIZE = 200;

    /**
     *  将较长的数组截断成较短的数组进行批处理
     */
    public static <T> void dealForSubList(List<T> totalList, int batchSize, Consumer<List<T>> subFunc) {
        if (CollectionUtils.isEmpty(totalList)) {
            return;
        }
        List<T> dealList = new ArrayList<>(totalList);
        while (dealList.size() > batchSize) {
            List<T> subList = dealList.subList(0, batchSize);
            subFunc.accept(subList);
            dealList = dealList.subList(subList.size(), dealList.size());
        }
        if (CollectionUtils.isNotEmpty(dealList)) {
            subFunc.accept(dealList);
        }
    }


    public static <K, V> void dealForSubMap(Map<K, V> totalMap, int batchSize, Consumer<Map<K, V>> subFunc) {
        if (MapUtils.isEmpty(totalMap)) {
            return;
        }

        Map<K, V> dealMap = new LinkedHashMap<>(totalMap);
        while (dealMap.size() > batchSize) {
            Map<K, V> subMap = new LinkedHashMap<>();
            dealMap.forEach((k, v) -> {
                if (subMap.size() < batchSize) {
                    subMap.put(k, v);
                }
            });
            subFunc.accept(subMap);
            subMap.forEach(dealMap::remove);
        }
        if (MapUtils.isNotEmpty(dealMap)) {
            subFunc.accept(dealMap);
        }
    }

}
