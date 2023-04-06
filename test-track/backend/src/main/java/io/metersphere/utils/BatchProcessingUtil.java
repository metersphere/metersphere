package io.metersphere.utils;

import io.metersphere.base.domain.TestCaseTest;
import io.metersphere.base.domain.TestCaseTestExample;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 批量处理工具
 */
public class BatchProcessingUtil {

    private static final int BATCH_PROCESS_QUANTITY = 100;

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
}
