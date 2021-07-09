package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldExample;
import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.ext.ExtCustomFieldMapper;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.QueryCustomFieldRequest;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldService {

    @Resource
    ExtCustomFieldMapper extCustomFieldMapper;

    @Resource
    CustomFieldMapper customFieldMapper;

    @Lazy
    @Resource
    TestCaseTemplateService testCaseTemplateService;

    @Lazy
    @Resource
    IssueTemplateService issueTemplateService;

    @Lazy
    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    public String add(CustomField customField) {
        checkExist(customField);
        customField.setId(UUID.randomUUID().toString());
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
        customField.setGlobal(false);
        customField.setCreateUser(SessionUtils.getUserId());
        customFieldMapper.insert(customField);
        return customField.getId();
    }

    /**
     * 系统字段默认是查询的 workspace_id 为 null 的系统字段
     * 如果创建了对应工作空间的系统字段，则过滤掉重复的 workspace_id 为 null 的系统字段
     *
     * @param request
     * @return
     */
    public List<CustomField> list(QueryCustomFieldRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extCustomFieldMapper.list(request);
    }

    public Pager<List<CustomField>> listRelate(int goPage, int pageSize, QueryCustomFieldRequest request) {
        List<String> templateContainIds = request.getTemplateContainIds();
        if (CollectionUtils.isEmpty(templateContainIds)) {
            templateContainIds = new ArrayList<>();
        }
        request.setTemplateContainIds(templateContainIds);
        Page<List<CustomField>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, extCustomFieldMapper.listRelate(request));
    }

    public void delete(String id) {
        customFieldMapper.deleteByPrimaryKey(id);
        customFieldTemplateService.deleteByFieldId(id);
    }

    public void update(CustomField customField) {
        if (customField.getGlobal() != null && customField.getGlobal()) {
            // 如果是全局字段，则创建对应工作空间字段
            add(customField);
            if (StringUtils.equals(customField.getScene(), TemplateConstants.FieldTemplateScene.TEST_CASE.name())) {
                testCaseTemplateService.handleSystemFieldCreate(customField);
            } else {
                issueTemplateService.handleSystemFieldCreate(customField);
            }
        } else {
            checkExist(customField);
            customField.setUpdateTime(System.currentTimeMillis());
            customFieldMapper.updateByPrimaryKeySelective(customField);
        }
    }

    public List<CustomField> getGlobalField(String scene) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andGlobalEqualTo(true)
                .andSceneEqualTo(scene);
        return customFieldMapper.selectByExampleWithBLOBs(example);
    }

    public List<CustomFieldDao> getCustomFieldByTemplateId(String templateId) {
        List<CustomFieldTemplate> customFields = customFieldTemplateService.getCustomFields(templateId);
        List<String> fieldIds = customFields.stream()
                .map(CustomFieldTemplate::getFieldId)
                .collect(Collectors.toList());

        List<CustomField> fields = getFieldByIds(fieldIds);
        Map<String, CustomField> fieldMap = fields.stream()
                .collect(Collectors.toMap(CustomField::getId, item -> item));

        List<CustomFieldDao> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields.forEach((item) -> {
                CustomFieldDao customFieldDao = new CustomFieldDao();
                CustomField customField = fieldMap.get(item.getFieldId());
                BeanUtils.copyBean(customFieldDao, customField);
                BeanUtils.copyBean(customFieldDao, item);
                result.add(customFieldDao);
            });
        }
        return result;
    }

    public List<CustomField> getFieldByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            CustomFieldExample example = new CustomFieldExample();
            example.createCriteria()
                    .andIdIn(ids);
            return customFieldMapper.selectByExampleWithBLOBs(example);
        }
        return new ArrayList<>();
    }

    public List<CustomField> getDefaultField(QueryCustomFieldRequest request) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andSystemEqualTo(true)
                .andSceneEqualTo(request.getScene())
                .andWorkspaceIdEqualTo(request.getWorkspaceId());
        List<CustomField> workspaceSystemFields = customFieldMapper.selectByExampleWithBLOBs(example);
        Set<String> workspaceSystemFieldNames = workspaceSystemFields.stream()
                .map(CustomField::getName)
                .collect(Collectors.toSet());
        List<CustomField> globalFields = getGlobalField(request.getScene());
        // 工作空间的系统字段加上全局的字段
        globalFields.forEach(item -> {
            if (!workspaceSystemFieldNames.contains(item.getName())) {
                workspaceSystemFields.add(item);
            }
        });
        return workspaceSystemFields;
    }

    public CustomField getGlobalFieldByName(String name) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria()
                .andGlobalEqualTo(true)
                .andNameEqualTo(name);
        List<CustomField> customFields = customFieldMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(customFields)) {
            return customFields.get(0);
        }
        return null;
    }

    public List<String> listIds(QueryCustomFieldRequest request) {
        return extCustomFieldMapper.listIds(request);
    }

    private void checkExist(CustomField customField) {
        if (customField.getName() != null) {
            CustomFieldExample example = new CustomFieldExample();
            CustomFieldExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(customField.getName());
            criteria.andWorkspaceIdEqualTo(customField.getWorkspaceId());
            if (StringUtils.isNotBlank(customField.getId())) {
                criteria.andIdNotEqualTo(customField.getId());
            }
            if (customFieldMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("custom_field_already") + customField.getName());
            }
        }
    }

    public String getLogDetails(String id) {
        CustomField customField = customFieldMapper.selectByPrimaryKey(id);
        if (customField != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(customField, SystemReference.fieldColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(customField.getId()), null, customField.getName(), customField.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
