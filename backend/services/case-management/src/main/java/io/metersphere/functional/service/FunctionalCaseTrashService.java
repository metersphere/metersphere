package io.metersphere.functional.service;

import io.metersphere.functional.constants.CaseEvent;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.domain.FunctionalCaseCustomFieldExample;
import io.metersphere.functional.domain.FunctionalCaseExample;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseBatchRequest;
import io.metersphere.provider.BaseCaseProvider;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.mapper.CustomFieldMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guoyuqi
 * 功能用例回收站服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseTrashService {
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;
    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private DeleteFunctionalCaseService deleteFunctionalCaseService;
    @Resource
    private BaseCaseProvider provider;

    /**
     * 从回收站恢复用例
     *
     * @param id     用例ID
     * @param userId 当前操作人
     */
    public void recoverCase(String id, String userId) {
        //检查并恢复所有版本
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        if (functionalCase == null) {
            throw new MSException(Translator.get("case_comment.case_is_null"));
        }
        List<String> ids = getIdsByRefId(functionalCase.getRefId());
        //检查自定义字段是否还存在，不存在，删除关联关系（与恢复流程没关系可异步执行）
        delCustomFields(ids);
        extFunctionalCaseMapper.recoverCase(ids, userId, System.currentTimeMillis());
        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.CASE_IDS, ids);
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.RECOVER_FUNCTIONAL_CASE);
        provider.updateCaseReview(param);
    }

    /**
     * 检查自定义字段是否还存在，不存在，删除关联关系（与恢复流程没关系可异步执行）
     */
    public void delCustomFields(List<String> ids) {
        doDeleteCustomFields(ids);
    }

    private void doDeleteCustomFields(List<String> ids) {
        FunctionalCaseCustomFieldExample functionalCaseCustomFieldExample = new FunctionalCaseCustomFieldExample();
        functionalCaseCustomFieldExample.createCriteria().andCaseIdIn(ids);
        List<FunctionalCaseCustomField> functionalCaseCustomFields = functionalCaseCustomFieldMapper.selectByExample(functionalCaseCustomFieldExample);
        List<String> filedIds = functionalCaseCustomFields.stream().map(FunctionalCaseCustomField::getFieldId).distinct().toList();
        List<CustomField> customFields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filedIds)) {
            CustomFieldExample customFieldExample = new CustomFieldExample();
            customFieldExample.createCriteria().andIdIn(filedIds);
            customFields = customFieldMapper.selectByExample(customFieldExample);
        }
        List<String> customFieldIds = customFields.stream().map(CustomField::getId).toList();
        List<String> delIds = filedIds.stream().filter(t -> !customFieldIds.contains(t)).toList();
        if (CollectionUtils.isNotEmpty(delIds)) {
            functionalCaseCustomFieldExample = new FunctionalCaseCustomFieldExample();
            functionalCaseCustomFieldExample.createCriteria().andFieldIdIn(delIds);
            functionalCaseCustomFieldMapper.deleteByExample(functionalCaseCustomFieldExample);
        }
    }


    public void delCustomFieldsByRefIds(List<String> refIds) {
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andRefIdIn(refIds);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        List<String> ids = functionalCases.stream().map(FunctionalCase::getId).toList();
        doDeleteCustomFields(ids);
    }

    /**
     * 从回收站彻底删除用例
     *
     * @param id 用例ID
     */
    public void deleteCase(String id, String userId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        if (functionalCase == null) {
            return;
        }
        List<String> ids = getIdsByRefId(functionalCase.getRefId());
        deleteByIds(functionalCase.getProjectId(), ids, userId);
    }

    private List<String> getIdsByRefId(String refId) {
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andRefIdEqualTo(refId);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        return functionalCases.stream().map(FunctionalCase::getId).toList();
    }

    /**
     * 从回收站批量回复用例(恢复就是恢复所有版本)
     *
     * @param request request
     * @param userId  userId
     */
    public void batchRecoverCase(FunctionalCaseBatchRequest request, String userId) {
        List<String> refIds;
        if (request.isSelectAll()) {
            //回收站全部恢复
            List<String> ids = getIds(request);
            if (CollectionUtils.isEmpty(ids)) return;
            refIds = extFunctionalCaseMapper.getRefIds(ids, true);
        } else {
            if (CollectionUtils.isEmpty(request.getSelectIds())) {
                return;
            }
            refIds = extFunctionalCaseMapper.getRefIds(request.getSelectIds(), true);
        }
        if (CollectionUtils.isEmpty(refIds)) {
            return;
        }
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andRefIdIn(refIds);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        List<String> ids = functionalCases.stream().map(FunctionalCase::getId).toList();
        extFunctionalCaseMapper.recoverCaseByRefIds(refIds, userId, System.currentTimeMillis());
        delCustomFieldsByRefIds(refIds);
        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.CASE_IDS, CollectionUtils.isNotEmpty(ids) ? ids : new ArrayList<>());
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.RECOVER_FUNCTIONAL_CASE);
        provider.updateCaseReview(param);
    }

    /**
     * 获取实际的选择ID
     *
     * @param request request
     * @return List<String>
     */
    private List<String> getIds(FunctionalCaseBatchRequest request) {
        List<String> ids = extFunctionalCaseMapper.getIds(request, request.getProjectId(), true);
        if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
            ids.removeAll(request.getExcludeIds());
        }
        return ids;
    }

    /**
     * 批量彻底删除，也分当前版本和全部版本
     *
     * @param request request
     */
    public void batchDeleteCase(FunctionalCaseBatchRequest request, String userId) {
        if (request.isSelectAll()) {
            //判断是否全部删除
            List<String> ids = getIds(request);
            if (CollectionUtils.isEmpty(ids)) return;
            doDeleteBatchCase(request, ids, userId);
        } else {
            List<String> selectIds = request.getSelectIds();
            if (CollectionUtils.isEmpty(selectIds)) {
                return;
            }
            doDeleteBatchCase(request, selectIds, userId);
        }
    }

    private void doDeleteBatchCase(FunctionalCaseBatchRequest request, List<String> ids, String userId) {
        if (request.getDeleteAll()) {
            //回收站全部版本全都删除
            List<String> refIds = extFunctionalCaseMapper.getRefIds(ids, true);
            FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
            functionalCaseExample.createCriteria().andRefIdIn(refIds).andDeletedEqualTo(true);
            List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
            List<String> deleteIds = functionalCases.stream().map(FunctionalCase::getId).toList();
            deleteByIds(request.getProjectId(), deleteIds, userId);
        } else {
            //只删除当前选择的数据
            deleteByIds(request.getProjectId(), ids, userId);
        }
    }

    public void deleteByIds(String projectId, List<String> deleteIds, String userId) {
        deleteFunctionalCaseService.deleteFunctionalCaseResource(deleteIds, projectId);
        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.CASE_IDS, deleteIds);
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.DELETE_TRASH_FUNCTIONAL_CASE);
        provider.updateCaseReview(param);
    }
}
