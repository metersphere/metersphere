package io.metersphere.track.request.testreview;

import io.metersphere.base.domain.TestCaseReviewApiCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
@Getter
@Setter
public class TestReviewApiCaseBatchRequest extends TestCaseReviewApiCase {
    private List<String> ids;

    /**
     * 批量修改选中的数据
     */
    private Map<String, String> selectRows;

    /**
     * 项目ID，环境ID对应关系
     */
    private Map<String, String> projectEnvMap;
}
