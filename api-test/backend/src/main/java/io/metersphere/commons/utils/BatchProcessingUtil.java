package io.metersphere.commons.utils;

import io.metersphere.api.dto.automation.ScenarioProjectDTO;
import io.metersphere.api.dto.definition.BatchDataCopyRequest;
import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;
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

    public static ScenarioProjectDTO getProjectIdsByScenarioIdList(List<String> scenarioIdList, Function<List<String>, ScenarioProjectDTO> func) {
        ScenarioProjectDTO returnDTO = new ScenarioProjectDTO();
        if (CollectionUtils.isNotEmpty(scenarioIdList)) {
            int unProcessingCount = scenarioIdList.size();
            while (scenarioIdList.size() > BATCH_PROCESS_QUANTITY) {
                List<String> processingList = new ArrayList<>();
                for (int i = 0; i < BATCH_PROCESS_QUANTITY; i++) {
                    processingList.add(scenarioIdList.get(i));
                }
                //函数处理
                returnDTO.merge(func.apply(processingList));

                scenarioIdList.removeAll(processingList);
                //如果剩余数量没有发生变化，则跳出循环。防止出现死循环的情况
                if (scenarioIdList.size() == unProcessingCount) {
                    break;
                } else {
                    unProcessingCount = scenarioIdList.size();
                }
            }
            if (CollectionUtils.isNotEmpty(scenarioIdList)) {
                //剩余待处理数据进行处理
                returnDTO.merge(func.apply(scenarioIdList));
            }
        }
        return returnDTO;
    }

    public static List<ApiScenarioReportResultWithBLOBs> selectScenarioReportResultByScenarioReportId(List<String> scenarioReportId, Function<List<String>, List<ApiScenarioReportResultWithBLOBs>> func) {
        List<ApiScenarioReportResultWithBLOBs> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(scenarioReportId)) {
            int unProcessingCount = scenarioReportId.size();
            while (scenarioReportId.size() > BATCH_PROCESS_QUANTITY) {
                List<String> processingList = new ArrayList<>();
                for (int i = 0; i < BATCH_PROCESS_QUANTITY; i++) {
                    processingList.add(scenarioReportId.get(i));
                }
                //函数处理
                returnList.addAll(func.apply(processingList));

                scenarioReportId.removeAll(processingList);
                if (scenarioReportId.size() == unProcessingCount) {
                    //如果剩余数量没有发生变化，则跳出循环。防止出现死循环的情况
                    break;
                } else {
                    unProcessingCount = scenarioReportId.size();
                }
            }
            if (CollectionUtils.isNotEmpty(scenarioReportId)) {
                //剩余待处理数据进行处理
                returnList.addAll(func.apply(scenarioReportId));
            }
        }
        return returnList;
    }

    public static void batchProcessingByDataCopy(BatchDataCopyRequest paramRequest, Consumer<BatchDataCopyRequest> func) {
        List<String> paramList = paramRequest.getIds();
        if (CollectionUtils.isNotEmpty(paramList)) {
            BatchDataCopyRequest queryRequest = new BatchDataCopyRequest();
            BeanUtils.copyBean(queryRequest, paramRequest);
            int unProcessingCount = paramList.size();
            while (paramList.size() > BATCH_PROCESS_QUANTITY) {
                List<String> processingList = new ArrayList<>();
                for (int i = 0; i < BATCH_PROCESS_QUANTITY; i++) {
                    processingList.add(paramList.get(i));
                }
                //函数处理
                queryRequest.setIds(processingList);
                func.accept(queryRequest);

                paramList.removeAll(processingList);
                if (paramList.size() == unProcessingCount) {
                    //如果剩余数量没有发生变化，则跳出循环。防止出现死循环的情况
                    break;
                } else {
                    unProcessingCount = paramList.size();
                }
            }

            if (CollectionUtils.isNotEmpty(paramList)) {
                //剩余待处理数据进行处理
                queryRequest.setIds(paramList);
                func.accept(queryRequest);
            }
        }
    }
}
