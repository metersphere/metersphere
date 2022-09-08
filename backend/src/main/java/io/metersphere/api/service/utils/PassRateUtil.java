package io.metersphere.api.service.utils;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.api.dto.StepTreeDTO;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.service.ApiScenarioReportStructureService;
import io.metersphere.base.domain.*;
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

    private final static String ZERO_PERCENT = "0%";
    public static final String UNEXECUTE = "unexecute";

    public static String calculatePassRate(List<ApiScenarioReportResultWithBLOBs> requestResults, ApiScenarioReport report) {
        if (CollectionUtils.isEmpty(requestResults)) {
            return ZERO_PERCENT;
        } else {
            long successSize = requestResults.stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ScenarioStatus.Success.name())).count();
            if (report.getReportType().startsWith(SystemConstants.TestTypeEnum.UI.name())) {
                //resourceId -> 结果列表 循环可能一个 resourceId 对应多个接口
                Map<String, List<ApiScenarioReportResultWithBLOBs>> resultResourceStatusMap = requestResults.stream().collect(Collectors.groupingBy(ApiScenarioReportResult::getResourceId));

                ApiScenarioReportStructureExample e = new ApiScenarioReportStructureExample();
                e.createCriteria().andReportIdEqualTo(report.getId());

                List<ApiScenarioReportStructureWithBLOBs> apiScenarioReportStructures = CommonBeanFactory.getBean(ApiScenarioReportStructureMapper.class).selectByExampleWithBLOBs(e);
                if (CollectionUtils.isEmpty(apiScenarioReportStructures)) {
                    return ZERO_PERCENT;
                }
                List<StepTreeDTO> stepList = JSONArray.parseArray(new String(apiScenarioReportStructures.get(0).getResourceTree(), StandardCharsets.UTF_8), StepTreeDTO.class);
                ((ApiScenarioReportStructureService) CommonBeanFactory.getBean("apiScenarioReportStructureService")).reportFormatting(stepList, resultResourceStatusMap, "UI");
                successSize = getUISuccessSize(stepList);

                if (CollectionUtils.isEmpty(stepList)) {
                    return ZERO_PERCENT;
                }
                return new DecimalFormat(ZERO_PERCENT).format((float) successSize / getTotalSteps(stepList));
            } else {
                return new DecimalFormat(ZERO_PERCENT).format((float) successSize / requestResults.size());
            }
        }
    }

    /**
     * 计算 UI 成功的步骤数
     *
     * @param stepList
     * @return
     */
    private static long getUISuccessSize(List<StepTreeDTO> stepList) {
        AtomicLong atomicLong = new AtomicLong();
        stepList.forEach(stepFather -> {
            stepFather.getChildren().forEach(step -> {
                //scenario 拥有 hashtree 的控制语句
                if (CollectionUtils.isNotEmpty(step.getChildren())) {
                    AtomicLong failCount = new AtomicLong();
                    AtomicLong unExecuteCount = new AtomicLong();
                    getUIFailStepCount(Optional.ofNullable(step.getChildren()).orElse(new ArrayList<>()), failCount);
                    getUIUnExecuteStepCount(Optional.ofNullable(step.getChildren()).orElse(new ArrayList<>()), unExecuteCount);

                    //复制或者嵌套场景的成功只算 1 次
                    if (failCount.get() == 0 && unExecuteCount.get() == 0) {
                        atomicLong.getAndAdd(1);
                    }
                } else {
                    calculateCount(atomicLong, step, ScenarioStatus.Success.name());
                }
            });
        });

        return atomicLong.get();
    }

    /**
     * 递归叶子节点，有失败的证明嵌套的场景是失败的
     *
     * @param stepTrees
     * @param count
     * @return
     */
    private static void getUIFailStepCount(List<StepTreeDTO> stepTrees, AtomicLong count) {
        stepTrees.forEach(step -> {
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                getUIFailStepCount(Optional.ofNullable(step.getChildren()).orElse(new ArrayList<>()), count);
            } else {
                calculateCount(count, step, ScenarioStatus.Fail.name());
            }
        });
    }

    private static void getUIUnExecuteStepCount(List<StepTreeDTO> stepTrees, AtomicLong count) {
        stepTrees.forEach(step -> {
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                getUIUnExecuteStepCount(Optional.ofNullable(step.getChildren()).orElse(new ArrayList<>()), count);
            } else {
                if (StringUtils.equalsIgnoreCase(step.getTotalStatus(), UNEXECUTE)) {
                    count.getAndIncrement();
                }
            }
        });
    }

    private static void calculateCount(AtomicLong count, StepTreeDTO step, String status) {
        if (StringUtils.equalsIgnoreCase(status, step.getTotalStatus())) {
            count.getAndIncrement();
        }
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
