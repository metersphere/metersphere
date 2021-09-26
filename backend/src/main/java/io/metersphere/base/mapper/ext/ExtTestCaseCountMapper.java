package io.metersphere.base.mapper.ext;

import io.metersphere.reportstatistics.dto.TestCaseCountChartResult;
import io.metersphere.reportstatistics.dto.TestCaseCountRequest;

import java.util.List;

public interface ExtTestCaseCountMapper {

    /**
     * 创建人 维护人 用例类型 用例状态 用例等级
     *
     * create_user
     * maintainer
     * '功能用例'
     * status
     * priority
     *
     * @ request
     * @return
     */
    List<TestCaseCountChartResult> getFunctionCaseCount(TestCaseCountRequest request);

    /**
     * 创建人 维护人 用例类型 用例状态 用例等级
     *
     * create_user_id
     *  ----不知道
     * '接口用例'
     * status
     *  ----不知道
     *
     * @param request
     * @return
     */
    List<TestCaseCountChartResult> getApiCaseCount(TestCaseCountRequest request);

    /**
     * 创建人 维护人 用例类型 用例状态 用例等级
     *
     * create_user
     * principal
     * '场景用例'
     * status
     * level
     *
     * @param request
     * @return
     */
    List<TestCaseCountChartResult> getScenarioCaseCount(TestCaseCountRequest request);

    /**
     * 创建人 维护人 用例类型 用例状态 用例等级
     *
     * create_user
     * follow_people
     * '性能用例'
     * status
     * 不知道
     *
     * @param request
     * @return
     */
    List<TestCaseCountChartResult> getLoadCaseCount(TestCaseCountRequest request);
}
