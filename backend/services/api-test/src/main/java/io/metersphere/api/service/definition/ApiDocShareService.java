package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDocShare;
import io.metersphere.api.domain.ApiDocShareExample;
import io.metersphere.api.dto.definition.ApiDocShareDTO;
import io.metersphere.api.dto.definition.ApiDocShareDetail;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.mapper.ApiDocShareMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiDocShareMapper;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author song-cc-rock
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDocShareService {

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

	public static final String RANGE_ALL = "ALL";

	/**
	 * 分页获取分享列表
	 * @param request 分页请求参数
	 * @return 分享列表
	 */
	public List<ApiDocShareDTO> list(ApiDocSharePageRequest request) {
		List<ApiDocShareDTO> list = extApiDocShareMapper.list(request);
		return buildApiShareExtra(list);
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
		apiDocShareMapper.insert(docShare);
		return docShare;
	}

	/**
	 * 更新分享
	 * @param request 请求参数
	 * @return 分享
	 */
	public ApiDocShare update(ApiDocShareEditRequest request) {
		checkExit(request.getId());
		checkDuplicateName(request);
		ApiDocShare docShare = new ApiDocShare();
		BeanUtils.copyBean(docShare, request);
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
		ApiDocShareDetail detail = ApiDocShareDetail.builder().allowExport(docShare.getAllowExport()).isPrivate(docShare.getIsPrivate()).build();
		if (docShare.getInvalidTime() == null || StringUtils.isBlank(docShare.getInvalidUnit())) {
			detail.setInvalid(false);
		} else {
			Long deadline = calculateDeadline(docShare.getInvalidTime(), docShare.getInvalidUnit(), docShare.getCreateTime());
			detail.setInvalid(deadline < System.currentTimeMillis());
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
	 * 构建分享额外信息
	 * @param docShares 分享列表
	 * @return 分享列表
	 */
	public List<ApiDocShareDTO> buildApiShareExtra(List<ApiDocShareDTO> docShares) {
		docShares.forEach(docShare -> {
			docShare.setDeadline(calculateDeadline(docShare.getInvalidTime(), docShare.getInvalidUnit(), docShare.getCreateTime()));
			docShare.setInvalid(docShare.getDeadline() != null && docShare.getDeadline() < System.currentTimeMillis());
			docShare.setApiShareNum(countApiShare(docShare));
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
	 * 计算截止时间
	 * @param val 时间值
	 * @param unit 时间单位
	 * @param stareTime 起始时间
	 * @return 截止时间
	 */
	private Long calculateDeadline(Integer val, String unit, Long stareTime) {
		if (val == null) {
			return null;
		}
		return switch (unit) {
			case "HOUR" -> stareTime + val * 60 * 60 * 1000L;
			case "DAY" -> stareTime + val * 24 * 60 * 60 * 1000L;
			case "MONTH" -> stareTime + val * 30 * 24 * 60 * 60 * 1000L;
			case "YEAR" -> stareTime + val * 365 * 24 * 60 * 60 * 1000L;
			default -> null;
		};
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
