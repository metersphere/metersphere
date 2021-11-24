package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestPlanTestCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestPlanApiCaseBatchRequest extends TestPlanTestCase {
    private List<String> ids;

    /**
     * 批量修改选中的数据
     */
    private Map<String, String> selectRows;

    /**
     * 项目ID，环境ID对应关系
     */
    private Map<String, String> projectEnvMap;

    private ApiCaseBatchCondition condition;
    private String environmentType;
    private String environmentGroupId;
}
