package io.metersphere.api.dto.definition;

import io.metersphere.sdk.util.CalculateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 */
@Data
@NoArgsConstructor
public class ApiCoverageDTO {

    @Schema(description = "接口定义总量")
    private int allApiCount;

    @Schema(description = "接口定义未覆盖")
    private int unCoverWithApiDefinition;

    @Schema(description = "接口定义已覆盖")
    private int coverWithApiDefinition;

    @Schema(description = "接口覆盖率  接口（URL）有（用例或场景步骤）数/接口总数*100%")
    private String apiCoverage;

    @Schema(description = "接口用例未覆盖")
    private int unCoverWithApiCase;

    @Schema(description = "接口用例已覆盖")
    private int coverWithApiCase;

    @Schema(description = "用例覆盖率  有用例的接口/接口总数*100%")
    private String apiCaseCoverage;

    @Schema(description = "接口场景未覆盖")
    private int unCoverWithApiScenario;

    @Schema(description = "接口场景已覆盖")
    private int coverWithApiScenario;

    @Schema(description = "场景覆盖率  被场景步骤包含的接口(URL)数/接口总数*100%")
    private String scenarioCoverage;

    public ApiCoverageDTO(List<String> allApiId, List<String> haveCaseIdList, List<String> apiIdOrUrlInStepList) {
        // 去重、过滤（只留下apiId中存在的数据，避免已删除的apiId导致统计错误）
        allApiId = allApiId.stream().distinct().toList();
        haveCaseIdList = this.elementInList(allApiId, haveCaseIdList.stream().distinct().toList());
        apiIdOrUrlInStepList = this.elementInList(allApiId, apiIdOrUrlInStepList.stream().distinct().toList());

        this.allApiCount = allApiId.size();
        // 用例覆盖率： 有用例的接口/接口总数*100%
        this.coverWithApiCase = haveCaseIdList.size();
        this.unCoverWithApiCase = this.allApiCount - this.coverWithApiCase;
        this.apiCaseCoverage = CalculateUtils.reportPercentage(coverWithApiCase, allApiCount);

        // 场景覆盖率： 被场景步骤包含的接口(URL)数/接口总数*100%
        this.coverWithApiScenario = apiIdOrUrlInStepList.size();
        this.unCoverWithApiScenario = this.allApiCount - this.coverWithApiScenario;
        this.scenarioCoverage = CalculateUtils.reportPercentage(coverWithApiScenario, allApiCount);

        // 接口覆盖率
        apiIdOrUrlInStepList.addAll(haveCaseIdList);
        List<String> allCoverList = apiIdOrUrlInStepList.stream().distinct().toList();
        this.coverWithApiDefinition = allCoverList.size();
        this.unCoverWithApiDefinition = this.allApiCount - this.coverWithApiDefinition;
        this.apiCoverage = CalculateUtils.reportPercentage(coverWithApiDefinition, allApiCount);
    }

    private List<String> elementInList(List<String> allList, List<String> compareList) {
        List<String> returnList = new ArrayList<>();
        compareList.forEach(item -> {
            if (allList.contains(item)) {
                returnList.add(item);
            }
        });
        return returnList;
    }
}
