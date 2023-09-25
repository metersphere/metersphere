package io.metersphere.sdk.util;

import java.util.List;
import java.util.function.Consumer;

public class SubListUtil {

    /**
     *  将较长的数组截断成较短的数组进行批处理
     */
    public static void dealForSubList(List totalList, Integer batchSize, Consumer<List> subFunc) {
        int count = totalList.size();
        int iteratorCount = count / batchSize;
        for (int i = 0; i <= iteratorCount; i++) {
            int endIndex, startIndex;
            startIndex = i * batchSize;
            endIndex = ((endIndex = (i + 1) * batchSize) > count) ? count : endIndex;
            if (endIndex == startIndex) {
                break;
            }
            List subList = totalList.subList(startIndex, endIndex);
            subFunc.accept(subList);
        }
    }

}
