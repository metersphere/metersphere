package io.metersphere.sdk.util;

import io.metersphere.sdk.dto.api.result.RequestResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 重试报告处理util
 */
public class RetryResultUtils {
    public final static String RETRY = "MsRetry_";
    public final static String RETRY_CN = "RETRY";
    public final static String RETRY_FIRST_CN = "RETRY_FIRST";
    public final static String MS_CLEAR_LOOPS_VAR = "MS_CLEAR_LOOPS_VAR_";
    public final static int RETRY_RES_NUM = 11;

    /**
     * 合并掉重试结果；保留最后十次重试结果
     */
    public static void mergeRetryResults(List<RequestResult> results) {
        if (CollectionUtils.isNotEmpty(results)) {
            Map<String, List<RequestResult>> resultMap = results.stream().collect(Collectors.groupingBy(RequestResult::getResourceId));
            List<RequestResult> list = new LinkedList<>();
            resultMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    // 校验是否含重试结果
                    List<RequestResult> isRetryResults = v
                            .stream()
                            .filter(c -> StringUtils.isNotEmpty(c.getName()) && c.getName().startsWith(RETRY))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(isRetryResults)) {
                        // 取最后执行的10 条
                        if (v.size() > 10) {
                            v.sort(Comparator.comparing(RequestResult::getResourceId));
                            RequestResult sampleResult = v.getFirst();
                            List<RequestResult> topTens = v.subList(v.size() - RETRY_RES_NUM, v.size());
                            topTens.set(0, sampleResult);
                            assembleName(topTens);
                            list.addAll(topTens);
                        } else {
                            assembleName(v);
                            list.addAll(v);
                        }
                    } else {
                        // 成功的结果
                        list.addAll(v);
                    }
                }
            });
            results.clear();
            results.addAll(list);
        }
    }

    private static void assembleName(List<RequestResult> list) {
        // 名称排序处理
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setName(list.get(i).getName().replaceAll(RETRY, RETRY_CN));
            if (list.get(i).getName().endsWith("_")) {
                list.get(i).setName(list.get(i).getName().substring(0, list.get(i).getName().length() - 1));
            }
            if (i == 0) {
                list.get(i).setName(StringUtils.isNotEmpty(list.get(i).getName())
                        ? RETRY_FIRST_CN + "_" + list.get(i).getName() : RETRY_FIRST_CN);
            }
        }
    }

    public static List<SampleResult> clearLoops(List<SampleResult> results) {
        if (CollectionUtils.isNotEmpty(results)) {
            return results.stream().filter(sampleResult ->
                            StringUtils.isNotEmpty(sampleResult.getSampleLabel())
                                    && !sampleResult.getSampleLabel().startsWith(MS_CLEAR_LOOPS_VAR))
                    .collect(Collectors.toList());
        }
        return results;
    }
}
