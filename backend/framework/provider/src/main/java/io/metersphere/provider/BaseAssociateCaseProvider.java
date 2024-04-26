package io.metersphere.provider;

import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;

import java.util.List;

/**
 * 多个实现(关联用例基础接口)
 */
public interface BaseAssociateCaseProvider {

    /**
     * 获取尚未关联的列表
     *
     * @param testCasePageProviderRequest 用例查询条件
     * @return 通用用例返回集合
     */
    List<TestCaseProviderDTO> listUnRelatedTestCaseList(TestCasePageProviderRequest testCasePageProviderRequest);

    /**
     * 根据关联条件获取关联的用例ID
     * @param request 请求参数
     * @param deleted 是否删除状态
     * @return 用例ID集合
     */
    List<String> getRelatedIdsByParam(AssociateOtherCaseRequest request, boolean deleted);
}
