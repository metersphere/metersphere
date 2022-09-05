package io.metersphere.api.service.utils;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.api.dto.StepTreeDTO;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.domain.ApiScenarioReportResult;
import io.metersphere.base.domain.ApiScenarioReportStructureExample;
import io.metersphere.base.domain.ApiScenarioReportStructureWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioReportStructureMapper;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.SystemConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * API/UI/PERFORMANCE 通过率计算工具
 */
public class PassRateUtil {

    private final static String UISCENARIO_TYPE_NAME = "scenario";

    public static String calculatePassRate(List<ApiScenarioReportResult> requestResults, ApiScenarioReport report) {
        if (CollectionUtils.isEmpty(requestResults)) {
            return "0";
        } else {
            long successSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Success.name())).count();
            if (report.getReportType().startsWith(SystemConstants.TestTypeEnum.UI.name())) {
                Map<String, String> resultIdMap = requestResults.stream().collect(Collectors.toMap(r -> r.getResourceId(), r -> r.getStatus()));
                ApiScenarioReportStructureExample e = new ApiScenarioReportStructureExample();
                e.createCriteria().andReportIdEqualTo(report.getId());

                List<ApiScenarioReportStructureWithBLOBs> apiScenarioReportStructures = CommonBeanFactory.getBean(ApiScenarioReportStructureMapper.class).selectByExampleWithBLOBs(e);
                if (CollectionUtils.isEmpty(apiScenarioReportStructures)) {
                    return "0";
                }
                List<StepTreeDTO> stepList = JSONArray.parseArray(new String(apiScenarioReportStructures.get(0).getResourceTree(), StandardCharsets.UTF_8), StepTreeDTO.class);
                successSize = getUISuccessSize(resultIdMap, stepList);

                if (CollectionUtils.isEmpty(stepList)) {
                    return "0";
                }
                return new DecimalFormat("0%").format((float) successSize / getTotalSteps(stepList));
            } else {
                return new DecimalFormat("0%").format((float) successSize / requestResults.size());
            }
        }
    }

    /**
     * 计算 UI 成功的步骤数
     *
     * @param resultIdMap
     * @param stepList
     * @return
     */
    private static long getUISuccessSize(Map<String, String> resultIdMap, List<StepTreeDTO> stepList) {
        AtomicLong atomicLong = new AtomicLong();
        stepList.forEach(stepFather -> {
            stepFather.getChildren().forEach(step -> {
                if (StringUtils.equalsIgnoreCase(step.getType(), UISCENARIO_TYPE_NAME)) {
                    AtomicInteger failCount = new AtomicInteger();
                    getUIFailStepCount(resultIdMap, Optional.ofNullable(step.getChildren()).orElse(new ArrayList<>()), failCount);
                    //复制或者嵌套场景的成功只算 1 次
                    if (failCount.get() == 0) {
                        atomicLong.getAndAdd(1);
                    }
                } else {
                    if (resultIdMap.containsKey(step.getResourceId()) && StringUtils.equalsIgnoreCase(resultIdMap.get(step.getResourceId()), ScenarioStatus.Success.name())) {
                        atomicLong.incrementAndGet();
                    }
                }
            });
        });

        return atomicLong.get();
    }

    /**
     * 递归叶子节点，有失败的证明嵌套的场景是失败的
     *
     * @param stepTrees
     * @param resultIdMap
     * @param failCount
     * @return
     */
    private static void getUIFailStepCount(Map<String, String> resultIdMap, List<StepTreeDTO> stepTrees, AtomicInteger failCount) {
        stepTrees.forEach(step -> {
            if (StringUtils.equalsIgnoreCase(step.getType(), UISCENARIO_TYPE_NAME)) {
                getUIFailStepCount(resultIdMap, Optional.ofNullable(step.getChildren()).orElse(new ArrayList<>()), failCount);
            } else {
                if (resultIdMap.containsKey(step.getResourceId()) && StringUtils.equalsIgnoreCase(resultIdMap.get(step.getResourceId()), ScenarioStatus.Error.name())) {
                    failCount.incrementAndGet();
                }
            }
        });
    }

    /**
     * 计算总步骤数
     *
     * @param stepList
     * @return
     */
    private static float getTotalSteps(List<StepTreeDTO> stepList) {
        AtomicInteger integerAtomic = new AtomicInteger();
        stepList.forEach(s -> integerAtomic.getAndAdd(Optional.ofNullable(s.getChildren()).orElse(new ArrayList<>()).size()));
        return integerAtomic.get();
    }
}
