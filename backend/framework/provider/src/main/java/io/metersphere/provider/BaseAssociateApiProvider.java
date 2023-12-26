package io.metersphere.provider;

import io.metersphere.dto.ApiTestCaseProviderDTO;
import io.metersphere.request.ApiTestCasePageProviderRequest;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface BaseAssociateApiProvider {
    /**
     * 获取尚未关联的接口列表
     * @param sourceType 关联关系表表名
     * @param sourceName 关联关系表主动关联方字段名称
     * @param targetName 接口用例id 在关联关系表的字段名称
     * @param apiTestCasePageProviderRequest 接口用例高级搜索条件
     * @return List<ApiTestCaseProviderDTO>
     */
    List<ApiTestCaseProviderDTO> getApiTestCaseList(String sourceType,String sourceName, String targetName, ApiTestCasePageProviderRequest apiTestCasePageProviderRequest);
}
