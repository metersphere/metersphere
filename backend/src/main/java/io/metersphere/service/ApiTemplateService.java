package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTemplateMapper;
import io.metersphere.base.mapper.ext.ExtApiTemplateMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseTemplateMapper;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.UpdateApiFieldTemplateRequest;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.dto.ApiTemplateDTO;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.TestCaseTemplateDao;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTemplateService extends TemplateBaseService {

    @Resource
    ExtApiTemplateMapper extApiTemplateMapper;

    @Resource
    ApiTemplateMapper apiTemplateMapper;

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    @Resource
    CustomFieldService customFieldService;

    @Resource
    ProjectService projectService;

    public String add(UpdateApiFieldTemplateRequest request) {
        checkExist(request);
        ApiTemplate apiTemplate = new ApiTemplate();
        BeanUtils.copyBean(apiTemplate, request);
        apiTemplate.setId(UUID.randomUUID().toString());
        apiTemplate.setCreateTime(System.currentTimeMillis());
        apiTemplate.setUpdateTime(System.currentTimeMillis());
        apiTemplate.setGlobal(false);
        apiTemplate.setCreateUser(SessionUtils.getUserId());
        if (apiTemplate.getSystem() == null) {
            apiTemplate.setSystem(false);
        }
        request.setId(apiTemplate.getId());
        apiTemplateMapper.insert(apiTemplate);
        customFieldTemplateService.create(request.getCustomFields(), apiTemplate.getId(),
                TemplateConstants.FieldTemplateScene.API.name());
        return apiTemplate.getId();
    }

    public List<ApiTemplate> list(BaseQueryRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extApiTemplateMapper.list(request);
    }

    public void delete(String id) {
        checkTemplateUsed(id, projectService::getByApiTemplateId);
        apiTemplateMapper.deleteByPrimaryKey(id);
        customFieldTemplateService.deleteByTemplateId(id);
    }

    public void update(UpdateApiFieldTemplateRequest request) {
        if (request.getGlobal() != null && request.getGlobal()) {
            String originId = request.getId();
            // 如果是全局字段，则创建对应工作空间字段
            String id = add(request);
            projectService.updateApiTemplate(originId, id, request.getWorkspaceId());
        } else {
            checkExist(request);
            customFieldTemplateService.deleteByTemplateId(request.getId());
            ApiTemplate apiTemplate = new ApiTemplate();
            BeanUtils.copyBean(apiTemplate, request);
            apiTemplate.setUpdateTime(System.currentTimeMillis());
            apiTemplateMapper.updateByPrimaryKeySelective(apiTemplate);
            customFieldTemplateService.create(request.getCustomFields(), apiTemplate.getId(),
                    TemplateConstants.FieldTemplateScene.API.name());
        }
    }

    /**
     * 获取该工作空间的系统模板
     * - 如果没有，则创建该工作空间模板，并关联默认的字段
     * - 如果有，则更新原来关联的 fieldId
     *
     * @param customField
     */
    public void handleSystemFieldCreate(CustomField customField) {
        ApiTemplate workspaceSystemTemplate = getWorkspaceSystemTemplate(customField.getWorkspaceId());
        if (workspaceSystemTemplate == null) {
            createTemplateWithUpdateField(customField.getWorkspaceId(), customField);
        } else {
            updateRelateWithUpdateField(workspaceSystemTemplate, customField);
        }
    }

    private void createTemplateWithUpdateField(String workspaceId, CustomField customField) {
        UpdateApiFieldTemplateRequest request = new UpdateApiFieldTemplateRequest();
        ApiTemplate apiTemplate = new ApiTemplate();
        apiTemplate.setName("default");
        apiTemplate.setType(TemplateConstants.ApiTemplatePlatform.http.name());
        apiTemplate.setGlobal(false);
        apiTemplate.setSystem(true);
        apiTemplate.setWorkspaceId(workspaceId);
        BeanUtils.copyBean(request, apiTemplate);
        List<CustomFieldTemplate> systemFieldCreateTemplate =
                customFieldTemplateService.getSystemFieldCreateTemplate(customField, TemplateConstants.FieldTemplateScene.API.name());
        request.setCustomFields(systemFieldCreateTemplate);
        add(request);
    }

    private void updateRelateWithUpdateField(ApiTemplate template, CustomField customField) {
        CustomField globalField = customFieldService.getGlobalFieldByName(customField.getName());
        customFieldTemplateService.updateFieldIdByTemplate(template.getId(), globalField.getId(), customField.getId());
    }

    private ApiTemplate getWorkspaceSystemTemplate(String workspaceId) {
        ApiTemplateExample example = new ApiTemplateExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(workspaceId)
                .andSystemEqualTo(true);
        List<ApiTemplate> apiTemplates = apiTemplateMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTemplates)) {
            return apiTemplates.get(0);
        }
        return null;
    }

    private void checkExist(ApiTemplate apiTemplate) {
        if (null == apiTemplate.getName()) {
            return;
        }
        ApiTemplateExample example = new ApiTemplateExample();
        ApiTemplateExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(apiTemplate.getName())
                .andWorkspaceIdEqualTo(apiTemplate.getWorkspaceId());
        if (StringUtils.isNotBlank(apiTemplate.getId())) {
            criteria.andIdNotEqualTo(apiTemplate.getId());
        }
        if (apiTemplateMapper.selectByExample(example).size() > 0) {
            MSException.throwException(Translator.get("template_already") + apiTemplate.getName());
        }
    }

    public ApiTemplate getDefaultTemplate(String workspaceId) {
        ApiTemplateExample example = new ApiTemplateExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(workspaceId)
                .andSystemEqualTo(true);
        List<ApiTemplate> apiTemplates = apiTemplateMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTemplates)) {
            return apiTemplates.get(0);
        } else {
            example.clear();
            example.createCriteria()
                    .andGlobalEqualTo(true);
            return apiTemplateMapper.selectByExample(example).get(0);
        }
    }

    public List<ApiTemplate> getOption(String workspaceId) {
        ApiTemplateExample example = new ApiTemplateExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(workspaceId)
                .andSystemNotEqualTo(true);
        List<ApiTemplate> apiTemplates = apiTemplateMapper.selectByExample(example);
        apiTemplates.add(getDefaultTemplate(workspaceId));
        return apiTemplates;
    }


    public ApiTemplateDTO getTemplate(String projectId) {
        Project project = projectService.getProjectById(projectId);
        String apiTemplateId = project.getApiTemplateId();
        ApiTemplate apiTemplate = null;
        ApiTemplateDTO apiTemplateDTO = new ApiTemplateDTO();
        if (StringUtils.isNotBlank(apiTemplateId)) {
            apiTemplate = apiTemplateMapper.selectByPrimaryKey(apiTemplateId);
            if (apiTemplate == null) {
                apiTemplate = getDefaultTemplate(project.getWorkspaceId());
            }
        } else {
            apiTemplate = getDefaultTemplate(project.getWorkspaceId());
        }
        BeanUtils.copyBean(apiTemplateDTO, apiTemplate);
        List<CustomFieldDao> result = customFieldService.getCustomFieldByTemplateId(apiTemplate.getId());
        apiTemplateDTO.setCustomFields(result);
        return apiTemplateDTO;
    }

    public String getLogDetails(String id) {
        ApiTemplate apiTemplate = apiTemplateMapper.selectByPrimaryKey(id);
        if (apiTemplate != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(apiTemplate, SystemReference.apiFieldColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(apiTemplate.getId()), null, apiTemplate.getName(), apiTemplate.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
