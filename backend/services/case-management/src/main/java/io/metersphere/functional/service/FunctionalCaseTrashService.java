package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.domain.FunctionalCaseCustomFieldExample;
import io.metersphere.functional.domain.FunctionalCaseExample;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
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
import java.util.List;

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

    /**
     * 从回收站恢复用例
     * @param id 用例ID
     * @param userId 当前操作人
     */
    public void recoverCase(String id, String userId) {
        //检查并恢复所有版本
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        if (functionalCase == null) {
            throw new MSException(Translator.get("case_comment.case_is_null"));
        }
        List<String> ids = getIdsByRefId(functionalCase.getRefId());
        //检查自定义字段是否还存在，不存在，删除关联关系
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
        extFunctionalCaseMapper.recoverCase(ids,userId,System.currentTimeMillis());
    }

    /**
     * 从回收站彻底用例
     * @param id 用例ID
     */
    public void deleteCase(String id) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(id);
        if (functionalCase == null) {
           return;
        }
        List<String> ids = getIdsByRefId(functionalCase.getRefId());
        deleteFunctionalCaseService.deleteFunctionalCaseResource(ids, functionalCase.getProjectId());
    }

    private List<String> getIdsByRefId(String refId) {
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andRefIdEqualTo(refId);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        return functionalCases.stream().map(FunctionalCase::getId).toList();
    }
}
