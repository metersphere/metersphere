package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDocShare;
import io.metersphere.api.dto.definition.request.ApiDocShareEditRequest;
import io.metersphere.api.mapper.ApiDocShareMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDocShareLogService {

	@Resource
	private ApiDocShareMapper apiDocShareMapper;

	/**
	 * 添加接口分享日志
	 *
	 * @param request 请求参数
	 * @return 日志
	 */
	public LogDTO addLog(ApiDocShareEditRequest request) {
		LogDTO dto = new LogDTO(request.getProjectId(), null, null, null,OperationLogType.SHARE.name(), OperationLogModule.API_TEST_MANAGEMENT_DEFINITION_SHARE, request.getName());
		dto.setHistory(true);
		dto.setMethod(HttpMethodConstants.POST.name());
		dto.setOriginalValue(JSON.toJSONBytes(request));
		return dto;
	}

	/**
	 * 更新接口分享日志
	 *
	 * @param request 请求参数
	 * @return 日志
	 */
	public LogDTO updateLog(ApiDocShareEditRequest request) {
		LogDTO dto = new LogDTO(request.getProjectId(), null, null, null,OperationLogType.UPDATE.name(), OperationLogModule.API_TEST_MANAGEMENT_DEFINITION_SHARE, request.getName());
		dto.setHistory(true);
		dto.setMethod(HttpMethodConstants.POST.name());
		dto.setOriginalValue(JSON.toJSONBytes(request));
		return dto;
	}

	/**
	 * 删除接口分享日志
	 *
	 * @param id 分享ID
	 * @return 日志
	 */
	public LogDTO deleteLog(String id) {
		ApiDocShare docShare = apiDocShareMapper.selectByPrimaryKey(id);
		LogDTO dto = new LogDTO(docShare.getProjectId(), null, null, null,OperationLogType.DELETE.name(), OperationLogModule.API_TEST_MANAGEMENT_DEFINITION_SHARE, docShare.getName());
		dto.setHistory(true);
		dto.setMethod(HttpMethodConstants.POST.name());
		dto.setOriginalValue(JSON.toJSONBytes(docShare));
		return dto;
	}
}
