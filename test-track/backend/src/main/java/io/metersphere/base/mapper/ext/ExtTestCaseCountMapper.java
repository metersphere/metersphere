package io.metersphere.base.mapper.ext;


import io.metersphere.dto.TestCaseCountChartResult;
import io.metersphere.dto.TestCaseCountRequest;

import java.util.List;

public interface ExtTestCaseCountMapper {

    /**
     * 创建人 维护人 用例类型 用例状态 用例等级
     * <p>
     * create_user
     * maintainer
     * '功能用例'
     * status
     * priority
     *
     * @return
     * @ request
     */
    List<TestCaseCountChartResult> getFunctionCaseCount(TestCaseCountRequest request);
}
