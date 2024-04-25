package io.metersphere.utils;

import io.metersphere.base.domain.TestCaseTest;
import io.metersphere.base.domain.TestCaseTestExample;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 批量处理工具
 */
public class BatchProcessingUtil {

    private static final int BATCH_PROCESS_QUANTITY = 100;

    //批量处理参数是List<String>的函数
    public static void consumerByStringList(List<String> stringList, Consumer<List<String>> consumer) {
        if (CollectionUtils.isNotEmpty(stringList)) {
            int foreachIndex = 0;
            int foreachCount = stringList.size() / BATCH_PROCESS_QUANTITY;
            while (BATCH_PROCESS_QUANTITY < stringList.size() || (foreachIndex > foreachCount)) {
                List<String> handleList = stringList.subList(0, BATCH_PROCESS_QUANTITY);
                consumer.accept(handleList);
                stringList.removeAll(handleList);

                //记录循环次数,防止出现死循环
                foreachIndex++;
            }
            //处理剩余数据
            if (CollectionUtils.isNotEmpty(stringList)) {
                consumer.accept(stringList);
            }
        }
    }

    public static void batchDeleteApiReport(List<String> testPlanReportIdList, Consumer<List<String>> deleteApiCaseReportFunc, Consumer<List<String>> deleteScenarioReportFunc, Consumer<List<String>> deleteUiReportFunc) {
        if (CollectionUtils.isNotEmpty(testPlanReportIdList)) {
            int unDeleteReportIdCount = testPlanReportIdList.size();
            while (BATCH_PROCESS_QUANTITY < testPlanReportIdList.size()) {
                List<String> deleteReportIds = testPlanReportIdList.subList(0, BATCH_PROCESS_QUANTITY);
                deleteApiCaseReportFunc.accept(deleteReportIds);
                deleteScenarioReportFunc.accept(deleteReportIds);
                deleteUiReportFunc.accept(deleteReportIds);

                testPlanReportIdList.removeAll(deleteReportIds);

                //未删除的报告数量如果未减少，跳出。防止死循环
                if (testPlanReportIdList.size() >= unDeleteReportIdCount) {
                    break;
                } else {
                    unDeleteReportIdCount = testPlanReportIdList.size();
                }
            }
            //处理剩余数据
            if (CollectionUtils.isNotEmpty(testPlanReportIdList)) {
                deleteApiCaseReportFunc.accept(testPlanReportIdList);
                deleteScenarioReportFunc.accept(testPlanReportIdList);
                deleteUiReportFunc.accept(testPlanReportIdList);
            }
        }
    }

    public static List<TestCaseTest> selectTestCaseTestByPrimaryKey(List<String> primaryKeyList, Function<TestCaseTestExample, List<TestCaseTest>> func) {
        List<TestCaseTest> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(primaryKeyList)) {
            TestCaseTestExample example = new TestCaseTestExample();
            int unProcessingCount = primaryKeyList.size();
            while (primaryKeyList.size() > BATCH_PROCESS_QUANTITY) {
                example.clear();
                List<String> processingList = new ArrayList<>();
                for (int i = 0; i < BATCH_PROCESS_QUANTITY; i++) {
                    processingList.add(primaryKeyList.get(i));
                }
                //函数处理
                example.createCriteria().andTestCaseIdIn(processingList);
                returnList.addAll(func.apply(example));

                primaryKeyList.removeAll(processingList);
                if (primaryKeyList.size() == unProcessingCount) {
                    //如果剩余数量没有发生变化，则跳出循环。防止出现死循环的情况
                    break;
                } else {
                    unProcessingCount = primaryKeyList.size();
                }
            }
            if (CollectionUtils.isNotEmpty(primaryKeyList)) {
                example.clear();
                //剩余待处理数据进行处理
                example.createCriteria().andTestCaseIdIn(primaryKeyList);
                returnList.addAll(func.apply(example));
            }
        }
        return returnList;
    }

    public static <T> List<T> subList(List<T> paramList, int size) {
        if (CollectionUtils.isNotEmpty(paramList)) {
            if (paramList.size() > size) {
                return paramList.subList(0, size);
            } else {
                return paramList;
            }
        }
        return new ArrayList<>();
    }
}
