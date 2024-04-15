package io.metersphere.plugin.platform.dto.request;

import io.metersphere.plugin.platform.dto.response.TestCaseDemandDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DemandUpdateRequest {

    /**
     * 关联需求的用例信息
     * getDemandId 可以获取关联的需求ID
     * getOriginDemandId 可以获取修改前的需求ID
     */
    private TestCaseDemandDTO testCase;

    /**
     * 批量关联需求时会使用
     */
    private List<TestCaseDemandDTO> testCaseList;

    /**
     * 项目设置的配置项
     */
    private String projectConfig;
}
