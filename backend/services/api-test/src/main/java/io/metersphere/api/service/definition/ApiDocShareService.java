package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDocShare;
import io.metersphere.api.domain.ApiDocShareExample;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ApiDocShareDTO;
import io.metersphere.api.dto.definition.ApiDocShareDetail;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.mapper.ApiDocShareMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiDocShareMapper;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.service.UserToolService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author song-cc-rock
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDocShareService {

	@Resource
	private UserToolService userToolService;
	@Resource
	private ExtApiDefinitionMapper extApiDefinitionMapper;
	@Resource
	private ApiDocShareMapper apiDocShareMapper;
	@Resource
	private ExtApiDocShareMapper extApiDocShareMapper;
	@Resource
	private ApiDefinitionModuleService apiDefinitionModuleService;
	@Resource
	private ApiDefinitionExportService apiDefinitionExportService;
	@Resource
	private ApiDefinitionService apiDefinitionService;
	@Resource
	private ApiTestService apiTestService;

	public static final String RANGE_ALL = "ALL";
	@Autowired
	private ProjectMapper projectMapper;

	/**
	 * 分页获取分享列表
	 * @param request 分页请求参数
	 * @return 分享列表
	 */
	public List<ApiDocShareDTO> list(ApiDocSharePageRequest request) {
		List<ApiDocShareDTO> shareList = extApiDocShareMapper.list(request);
		if (CollectionUtils.isEmpty(shareList)) {
			return new ArrayList<>();
		}
		return buildApiShareExtra(shareList);
	}

	/**
	 * 创建分享
	 * @param request 请求参数
	 * @param currentUser 当前用户
	 * @return 分享
	 */
	public ApiDocShare create(ApiDocShareEditRequest request, String currentUser) {
		checkDuplicateName(request);
		ApiDocShare docShare = new ApiDocShare();
		BeanUtils.copyBean(docShare, request);
		docShare.setId(IDGenerator.nextStr());
		docShare.setCreateUser(currentUser);
		docShare.setCreateTime(System.currentTimeMillis());
		docShare.setUpdateUser(currentUser);
		docShare.setUpdateTime(System.currentTimeMillis());
		docShare.setInvalidTime(request.getInvalidTime() == 0 ? Long.MAX_VALUE : request.getInvalidTime());
		apiDocShareMapper.insert(docShare);
		return docShare;
	}

	/**
	 * 更新分享
	 * @param request 请求参数
	 * @return 分享
	 */
	public ApiDocShare update(ApiDocShareEditRequest request, String currentUser) {
		checkExit(request.getId());
		checkDuplicateName(request);
		ApiDocShare docShare = new ApiDocShare();
		BeanUtils.copyBean(docShare, request);
		docShare.setUpdateUser(currentUser);
		docShare.setUpdateTime(System.currentTimeMillis());
		docShare.setInvalidTime(request.getInvalidTime() == 0 ? Long.MAX_VALUE : request.getInvalidTime());
		apiDocShareMapper.updateByPrimaryKeySelective(docShare);
		return docShare;
	}

	/**
	 * 删除分享
	 * @param id 分享ID
	 */
	public void delete(String id) {
		checkExit(id);
		apiDocShareMapper.deleteByPrimaryKey(id);
	}

	/**
	 * 检查分享密码
	 * @param request 校验请求参数
	 * @return 是否正确
	 */
	public Boolean check(ApiDocShareCheckRequest request) {
		ApiDocShare docShare = checkExit(request.getDocShareId());
		if (StringUtils.isBlank(docShare.getPassword())) {
			return true;
		}
		return StringUtils.equals(docShare.getPassword(), request.getPassword());
	}

	/**
	 * 获取分享详情
	 * @param id 分享ID
	 * @return 分享详情
	 */
	public ApiDocShareDetail detail(String id) {
		ApiDocShare docShare = checkExit(id);
		Project project = projectMapper.selectByPrimaryKey(docShare.getProjectId());
		ApiDocShareDetail detail = ApiDocShareDetail.builder().allowExport(docShare.getAllowExport()).isPrivate(docShare.getIsPrivate()).projectName(project.getName()).build();
		if (docShare.getInvalidTime() == null || docShare.getInvalidTime() == Long.MAX_VALUE) {
			detail.setInvalid(false);
		} else {
			detail.setInvalid(docShare.getInvalidTime() < System.currentTimeMillis());
		}
		return detail;
	}

	/**
	 * 查询分享左侧模块树
	 * @param request 请求参数
	 * @return 模块树节点数量
	 */
	public List<BaseTreeNode> getShareTree(ApiDocShareModuleRequest request) {
		ApiDocShare docShare = checkExit(request.getShareId());
		return apiDefinitionModuleService.getTree(buildModuleParam(request, docShare), false, true);
	}

	/**
	 * 查询分享左侧模块树节点数量
	 * @param request 请求参数
	 * @return 模块树节点数量
	 */
	public Map<String, Long> getShareTreeCount(ApiDocShareModuleRequest request) {
		ApiDocShare docShare = checkExit(request.getShareId());
		return apiDefinitionModuleService.moduleCount(buildModuleParam(request, docShare), false);
	}

	/**
	 * 导出接口定义
	 * @param request 请求参数
	 * @param type 类型
	 * @param currentUser 当前用户
	 * @return 接口定义导出返回
	 */
	public String export(ApiDocShareExportRequest request, String type, String currentUser) {
		if (request.isSelectAll()) {
			ApiDocShare docShare = checkExit(request.getShareId());
			List<String> shareIds = getShareIdsByParam(docShare);
			request.setSelectAll(false);
			request.setSelectIds(shareIds);
		}
		return apiDefinitionExportService.exportApiDefinition(request, type, currentUser);
	}

	/**
	 * 获取接口定义的协议脚本
	 * @param id 接口定义ID
	 * @param orgId 组织ID
	 * @return 协议脚本
	 */
	public Object getApiProtocolScript(String id, String orgId) {
		ApiDefinitionDTO apiDefinitionDTO = apiDefinitionService.get(id, "admin");
		List<ProtocolDTO> protocols = apiTestService.getProtocols(orgId);
		List<ProtocolDTO> noHttpProtocols = protocols.stream().filter(protocol -> !StringUtils.equals(protocol.getProtocol(), "HTTP")).toList();
		Map<String, String> protocolMap = noHttpProtocols.stream().collect(Collectors.toMap(ProtocolDTO::getProtocol, ProtocolDTO::getPluginId));
		if (!protocolMap.containsKey(apiDefinitionDTO.getProtocol())) {
			return null;
		}
		return apiTestService.getApiProtocolScript(protocolMap.get(apiDefinitionDTO.getProtocol()));
	}

	/**
	 * 构建分享额外信息
	 * @param docShares 分享列表
	 * @return 分享列表
	 */
	public List<ApiDocShareDTO> buildApiShareExtra(List<ApiDocShareDTO> docShares) {
		List<String> distinctCreateUserIds = docShares.stream().map(ApiDocShareDTO::getCreateUser).distinct().toList();
		List<String> distinctUpdateUserIds = docShares.stream().map(ApiDocShareDTO::getUpdateUser).distinct().toList();
		Map<String, String> userMap = userToolService.getUserMapByIds(ListUtils.union(distinctCreateUserIds, distinctUpdateUserIds));
		docShares.forEach(docShare -> {
			if (docShare.getInvalidTime() == Long.MAX_VALUE) {
				docShare.setInvalidTime(0L);
			}
			docShare.setInvalid(docShare.getInvalidTime() != null && docShare.getInvalidTime() != 0 && docShare.getInvalidTime() < System.currentTimeMillis());
			docShare.setApiShareNum(countApiShare(docShare));
			docShare.setCreateUserName(userMap.get(docShare.getCreateUser()));
			docShare.setUpdateUserName(userMap.get(docShare.getUpdateUser()));

		});
		return docShares;
	}

	/**
	 * 统计接口访范围分享接口数量
	 * @param docShare 接口分享
	 * @return 数量
	 */
	public Integer countApiShare(ApiDocShareDTO docShare) {
		List<String> shareIds = getShareIdsByParam(docShare);
		return CollectionUtils.isEmpty(shareIds) ? 0 : shareIds.size();
	}

	/**
	 * 根据分享信息获取分享的定义ID集合
	 * @param docShare 分享信息
	 * @return 分享的定义ID集合
	 */
	public List<String> getShareIdsByParam(ApiDocShare docShare) {
		StringBuilder condition = new StringBuilder();
		if (!StringUtils.equals(docShare.getApiRange(), RANGE_ALL) && !StringUtils.isBlank(docShare.getRangeMatchVal())) {
			switch (docShare.getApiRange()) {
				case "MODULE" -> {
					String[] moduleIds = StringUtils.split(docShare.getRangeMatchVal(), ",");
					condition.append("module_id in (");
					for (String moduleId : moduleIds) {
						condition.append("\"").append(moduleId).append("\", ");
					}
					condition.replace(condition.lastIndexOf(","), condition.length() - 1, ")");
				}
				case "PATH" -> {
					if (StringUtils.equals(docShare.getRangeMatchSymbol(), MsAssertionCondition.EQUALS.name())) {
						condition.append("path = '").append(docShare.getRangeMatchVal()).append("'");
					} else {
						condition.append("path like \"%").append(docShare.getRangeMatchVal()).append("%\"");
					}
				}
				case "TAG" -> {
					condition.append("(1=2 ");
					String[] tags = StringUtils.split(docShare.getRangeMatchVal(), ",");
					for (String tag : tags) {
						condition.append("OR JSON_CONTAINS(tags, JSON_ARRAY(\"").append(tag).append("\"))");
					}
					condition.append(")");
				}
				default -> {
				}
			}
		}
		return extApiDefinitionMapper.getIdsByShareParam(docShare.getProjectId(), condition.toString());
	}

	/**
	 * 构建模块树查询参数
	 * @param request 查询参数
	 * @param docShare 分享对象
	 * @return 模块树查询参数
	 */
	public ApiDocShareModuleRequest buildModuleParam(ApiDocShareModuleRequest request, ApiDocShare docShare) {
		// 设置接口范围查询条件
		if (!StringUtils.equals(docShare.getApiRange(), RANGE_ALL) && !StringUtils.isBlank(docShare.getRangeMatchVal())) {
			CombineSearch combineSearch = new CombineSearch();
			switch (docShare.getApiRange()) {
				case "MODULE" -> {
					String[] moduleIds = StringUtils.split(docShare.getRangeMatchVal(), ",");
					CombineCondition condition = buildModuleCondition("moduleId", Arrays.asList(moduleIds), "IN");
					combineSearch.setConditions(List.of(condition));
				}
				case "PATH" -> {
					if (StringUtils.equals(docShare.getRangeMatchSymbol(), MsAssertionCondition.EQUALS.name())) {
						CombineCondition condition = buildModuleCondition("path", docShare.getRangeMatchVal(), MsAssertionCondition.EQUALS.name());
						combineSearch.setConditions(List.of(condition));
					} else {
						CombineCondition condition = buildModuleCondition("path", docShare.getRangeMatchVal(), MsAssertionCondition.CONTAINS.name());
						combineSearch.setConditions(List.of(condition));
					}
				}
				case "TAG" -> {
					// 暂时只有包含操作
					String[] tags = StringUtils.split(docShare.getRangeMatchVal(), ",");
					CombineCondition condition = buildModuleCondition("tags", Arrays.asList(tags), MsAssertionCondition.CONTAINS.name());
					combineSearch.setConditions(List.of(condition));
				}
				default -> {
				}
			}
			request.setCombineSearch(combineSearch);
		}
		return request;
	}

	/**
	 * 是否存在
	 * @param id 分享ID
	 */
	private ApiDocShare checkExit(String id) {
		ApiDocShare docShare = apiDocShareMapper.selectByPrimaryKey(id);
		if (docShare == null) {
			throw new MSException(Translator.get("api_doc_share.not_exist"));
		}
		return docShare;
	}

	/**
	 * 检查分享名称是否重名
	 * @param request 请求参数
	 */
	private void checkDuplicateName(ApiDocShareEditRequest request) {
		ApiDocShareExample example = new ApiDocShareExample();
		ApiDocShareExample.Criteria criteria = example.createCriteria();
		criteria.andNameEqualTo(request.getName());
		criteria.andProjectIdEqualTo(request.getProjectId());
		if (StringUtils.isNotBlank(request.getId())) {
			criteria.andIdNotEqualTo(request.getId());
		}
		if (apiDocShareMapper.countByExample(example) > 0) {
			throw new MSException(Translator.get("api_doc_share.name_duplicate"));
		}
	}

	/**
	 * 组合左侧模块树的查询条件
	 * @param name 条件字段名
	 * @param val 条件字段值
	 * @param operator 操作符
	 * @return 组合查询条件
	 */
	private CombineCondition buildModuleCondition(String name, Object val, String operator) {
		CombineCondition condition = new CombineCondition();
		condition.setCustomField(false);
		condition.setCustomFieldType(StringUtils.EMPTY);
		condition.setName(name);
		condition.setValue(val);
		condition.setOperator(operator);
		return condition;
	}
}
