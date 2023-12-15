package io.metersphere.functional.service;


import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.CaseReviewFunctionalCase;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseExample;
import io.metersphere.functional.dto.ReviewFunctionalCaseDTO;
import io.metersphere.functional.dto.ReviewsDTO;
import io.metersphere.functional.mapper.CaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.ExtCaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.ExtCaseReviewFunctionalCaseUserMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseModuleMapper;
import io.metersphere.functional.request.BaseReviewCaseBatchRequest;
import io.metersphere.functional.request.CaseReviewFunctionalCasePosRequest;
import io.metersphere.functional.request.ReviewFunctionalCasePageRequest;
import io.metersphere.functional.utils.CaseListenerUtils;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用例评审和功能用例的中间表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CaseReviewFunctionalCaseService {


    @Resource
    private ExtCaseReviewFunctionalCaseMapper extCaseReviewFunctionalCaseMapper;
    @Resource
    private ExtFunctionalCaseModuleMapper extFunctionalCaseModuleMapper;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private ExtCaseReviewFunctionalCaseUserMapper extCaseReviewFunctionalCaseUserMapper;
    @Resource
    private CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    @Resource
    private CaseReviewService caseReviewService;

    /**
     * 通过评审id获取关联的用例id集合
     *
     * @param reviewId reviewId
     * @return String
     */
    public List<String> getCaseIdsByReviewId(String reviewId) {
        return extCaseReviewFunctionalCaseMapper.getCaseIdsByReviewId(reviewId);
    }


    /**
     * 评审详情分页列表查询
     *
     * @param request request
     * @param deleted deleted
     * @return ReviewFunctionalCaseDTO
     */
    public List<ReviewFunctionalCaseDTO> page(ReviewFunctionalCasePageRequest request, boolean deleted, String userId) {
        List<ReviewFunctionalCaseDTO> list = extCaseReviewFunctionalCaseMapper.page(request, deleted, userId, request.getSortString());
        return doHandleDTO(list, request.getReviewId());
    }

    private List<ReviewFunctionalCaseDTO> doHandleDTO(List<ReviewFunctionalCaseDTO> list, String reviewId) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> ids = list.stream().map(ReviewFunctionalCaseDTO::getId).toList();
            List<BaseTreeNode> modules = extFunctionalCaseModuleMapper.selectBaseByIds(ids);
            Map<String, String> moduleMap = modules.stream().collect(Collectors.toMap(BaseTreeNode::getId, BaseTreeNode::getName));

            List<String> versionIds = list.stream().map(ReviewFunctionalCaseDTO::getVersionId).toList();
            List<ProjectVersion> versions = extBaseProjectVersionMapper.getVersionByIds(versionIds);
            Map<String, String> versionMap = versions.stream().collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));

            List<ReviewsDTO> reviewers = extCaseReviewFunctionalCaseUserMapper.selectReviewers(ids, reviewId);
            Map<String, String> userIdMap = reviewers.stream().collect(Collectors.toMap(ReviewsDTO::getCaseId, ReviewsDTO::getUserIds));
            Map<String, String> userNameMap = reviewers.stream().collect(Collectors.toMap(ReviewsDTO::getCaseId, ReviewsDTO::getUserNames));

            list.forEach(item -> {
                item.setModuleName(moduleMap.get(item.getModuleId()));
                item.setVersionName(versionMap.get(item.getVersionId()));
                item.setReviewers(Collections.singletonList(userIdMap.get(item.getId())));
                item.setReviewNames(Collections.singletonList(userNameMap.get(item.getId())));
            });
        }
        return list;
    }

    /**
     * 批量删除
     *
     * @param request request
     */
    public void disassociate(BaseReviewCaseBatchRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            CaseReviewFunctionalCaseExample example = new CaseReviewFunctionalCaseExample();
            example.createCriteria().andIdIn(ids);
            Map<String, Object> param = getParam(request.getReviewId(), example, ids);
            CaseListenerUtils.addListener(param, CaseEvent.Event.BATCH_DISASSOCIATE);
            caseReviewFunctionalCaseMapper.deleteByExample(example);
        }
    }

    private Map<String, Object> getParam(String reviewId, CaseReviewFunctionalCaseExample example, List<String> ids) {
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = caseReviewFunctionalCaseMapper.selectByExample(example);
        List<String> caseIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getCaseId).toList();
        List<CaseReviewFunctionalCase> passList = caseReviewFunctionalCases.stream().filter(t -> !ids.contains(t.getId()) && StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.PASS.toString())).toList();
        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.CASE_IDS,caseIds);
        param.put(CaseEvent.Param.REVIEW_ID, reviewId);
        param.put(CaseEvent.Param.PASS_COUNT,passList.size());
        return param;
    }

    public List<String> doSelectIds(BaseReviewCaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extCaseReviewFunctionalCaseMapper.getIds(request, request.getUserId(), false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }


    /**
     * 评审详情页面 创建用例并关联
     *
     * @param caseId 功能用例ID
     * @param userId 当前操作人
     * @param reviewId 评审id
     */
    public void addCaseReviewFunctionalCase(String caseId, String userId, String reviewId) {
        CaseReviewFunctionalCase reviewFunctionalCase = new CaseReviewFunctionalCase();
        reviewFunctionalCase.setId(IDGenerator.nextStr());
        reviewFunctionalCase.setCaseId(caseId);
        reviewFunctionalCase.setReviewId(reviewId);
        reviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        reviewFunctionalCase.setCreateUser(userId);
        reviewFunctionalCase.setCreateTime(System.currentTimeMillis());
        reviewFunctionalCase.setUpdateTime(System.currentTimeMillis());
        reviewFunctionalCase.setPos(caseReviewService.getCaseFunctionalCaseNextPos(reviewId));
        caseReviewFunctionalCaseMapper.insertSelective(reviewFunctionalCase);

    }

    /**
     * 拖拽关联用例的排序
     */
    public void editPos(CaseReviewFunctionalCasePosRequest request) {
        ServiceUtils.updatePosField(request,
                CaseReviewFunctionalCase.class,
                caseReviewFunctionalCaseMapper::selectByPrimaryKey,
                extCaseReviewFunctionalCaseMapper::getPrePos,
                extCaseReviewFunctionalCaseMapper::getLastPos,
                caseReviewFunctionalCaseMapper::updateByPrimaryKeySelective);
    }
}