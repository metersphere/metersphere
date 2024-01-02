package io.metersphere.provider;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.request.AssociateCaseModuleProviderRequest;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;

import java.util.List;
import java.util.Map;

/**
 * @author guoyuqi
 */
public interface BaseAssociateApiProvider {
    /**
     * 获取尚未关联的接口列表
     *
     * @param sourceType                     关联关系表表名
     * @param sourceName                     关联关系表主动关联方字段名称
     * @param apiCaseColumnName              接口用例id 在关联关系表的字段名称
     * @param testCasePageProviderRequest 接口用例高级搜索条件
     * @return List<ApiTestCaseProviderDTO>
     */
    List<TestCaseProviderDTO> getApiTestCaseList(String sourceType, String sourceName, String apiCaseColumnName, TestCasePageProviderRequest testCasePageProviderRequest);

    /**
     * 根据接口用例的搜索条件获取符合条件的接口定义的模块统计数量
     *
     * @param request 接口用例高级搜索条件
     * @param deleted 接口定义是否删除
     * @return 接口模块统计数量
     */
    Map<String, Long> moduleCount(String sourceType, String sourceName, String apiCaseColumnName, AssociateCaseModuleProviderRequest request, boolean deleted);

    /**
     * 根据页面筛选条件获取批量操作的用例
     *
     * @return 接口用例的ids
     */
    List<ApiTestCase> getSelectApiTestCases(AssociateOtherCaseRequest request, Boolean deleted);


}
