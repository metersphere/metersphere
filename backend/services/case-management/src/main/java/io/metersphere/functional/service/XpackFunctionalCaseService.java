package io.metersphere.functional.service;

import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;

import java.util.List;

/**
 * 用例管理Xpack功能接口
 */
public interface XpackFunctionalCaseService {

    /**
     * 功能用例变更历史分页列表
     * @param request 请求参数
     * @return 变更历史集合
     */
    List<OperationHistoryDTO> listHis(OperationHistoryRequest request,String table);
}
