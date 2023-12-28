package io.metersphere.provider;

import io.metersphere.dto.ApiTestCaseProviderDTO;
import io.metersphere.request.ApiModuleProviderRequest;
import io.metersphere.request.ApiTestCasePageProviderRequest;

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
     * @param apiTestCasePageProviderRequest 接口用例高级搜索条件
     * @return List<ApiTestCaseProviderDTO>
     */
    List<ApiTestCaseProviderDTO> getApiTestCaseList(String sourceType, String sourceName, String apiCaseColumnName, ApiTestCasePageProviderRequest apiTestCasePageProviderRequest);

    /**
     * 根据接口用例的搜索条件获取符合条件的接口定义的模块统计数量
     *
     * @param request 接口用例高级搜索条件
     * @param deleted 接口定义是否删除
     * @return 接口模块统计数量
     */
    Map<String, Long> moduleCount(String sourceType, String sourceName, String apiCaseColumnName, ApiModuleProviderRequest request, boolean deleted);


}
