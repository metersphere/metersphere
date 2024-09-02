package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.dto.request.BugDeleteFileRequest;
import io.metersphere.bug.dto.request.BugUploadFileRequest;
import io.metersphere.bug.dto.response.BugFileDTO;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugAttachmentLogService {

	@Resource
	private BugMapper bugMapper;
	@Resource
	private BugAttachmentService bugAttachmentService;

	/**
	 * 更新缺陷-上传附件日志
	 *
	 * @param request 请求参数
	 * @param file  上传的文件
	 * @return 日志
	 */
	@SuppressWarnings("unused")
	public LogDTO uploadLog(BugUploadFileRequest request, MultipartFile file) {
		Bug bug = bugMapper.selectByPrimaryKey(request.getBugId());
		List<BugFileDTO> allBugFiles = bugAttachmentService.getAllBugFiles(request.getBugId());
		List<String> originalFileNames = allBugFiles.stream().map(BugFileDTO::getFileName).collect(Collectors.toList());
		LogDTO dto = buildUpdateLog(bug);
		dto.setModifiedValue(JSON.toJSONBytes(originalFileNames));
		originalFileNames.add(file.getOriginalFilename());
		dto.setOriginalValue(JSON.toJSONBytes(originalFileNames));
		return dto;
	}

	/**
	 * 更新缺陷-删除附件日志
	 *
	 * @param request 请求参数
	 * @return 日志
	 */
	@SuppressWarnings("unused")
	public LogDTO deleteLog(BugDeleteFileRequest request) {
		Bug bug = bugMapper.selectByPrimaryKey(request.getBugId());
		List<BugFileDTO> allBugFiles = bugAttachmentService.getAllBugFiles(request.getBugId());
		List<String> originalFileNames = allBugFiles.stream().map(BugFileDTO::getFileName).collect(Collectors.toList());
		BugFileDTO deleteFile;
		if (request.getAssociated()) {
			// 关联
			deleteFile = allBugFiles.stream().filter(file -> !file.getLocal() && StringUtils.equals(request.getRefId(), file.getRefId())).toList().getFirst();
		} else {
			// 本地
			deleteFile = allBugFiles.stream().filter(file -> file.getLocal() && StringUtils.equals(request.getRefId(), file.getRefId())).toList().getFirst();
		}
		LogDTO dto = buildUpdateLog(bug);
		dto.setModifiedValue(JSON.toJSONBytes(originalFileNames));
		originalFileNames.remove(deleteFile.getFileName());
		dto.setOriginalValue(JSON.toJSONBytes(originalFileNames));
		return dto;
	}

	/**
	 * 构建更新的缺陷日志对象
	 * @param bug 缺陷内容
	 * @return 日志对象
	 */
	private LogDTO buildUpdateLog(Bug bug) {
		LogDTO dto = new LogDTO(bug.getProjectId(), null, bug.getId(), null, OperationLogType.UPDATE.name(), OperationLogModule.BUG_MANAGEMENT_INDEX, bug.getTitle());
		dto.setHistory(true);
		dto.setPath("/bug/update");
		dto.setMethod(HttpMethodConstants.POST.name());
		return dto;
	}
}
