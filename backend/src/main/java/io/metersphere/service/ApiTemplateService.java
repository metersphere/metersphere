package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.ApiTemplate;
import io.metersphere.base.domain.ApiTemplateExample;
import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ApiTemplateMapper;
import io.metersphere.base.mapper.ext.ExtApiTemplateMapper;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.UpdateApiTemplateRequest;
import io.metersphere.dto.ApiTemplateDTO;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public String add(UpdateApiTemplateRequest request) {
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

    public void update(UpdateApiTemplateRequest request) {
        if (BooleanUtils.isTrue(request.getGlobal())) {
            String originId = request.getId();
            // 如果是全局字段，则创建对应工作空间字段
            String id = add(request);
            projectService.updateApiTemplate(originId, id, request.getProjectId());
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
    public void handleSystemFieldCreate(CustomFieldDao customField) {
        ApiTemplateExample example = new ApiTemplateExample();
        example.createCriteria()
                .andGlobalEqualTo(true);
        example.or(example.createCriteria()
                .andProjectIdEqualTo(customField.getProjectId()));
        List<ApiTemplate> apiTemplates = apiTemplateMapper.selectByExample(example);

        Map<Boolean, List<ApiTemplate>> templatesMap = apiTemplates.stream()
                .collect(Collectors.groupingBy(ApiTemplate::getGlobal));

        // 获取全局模板
        List<ApiTemplate> globalTemplates = templatesMap.get(true);
        // 获取当前工作空间下模板
        List<ApiTemplate> projectTemplates = templatesMap.get(false);

        globalTemplates.forEach(global -> {
            List<ApiTemplate> projectTemplate = null;
            if (CollectionUtils.isNotEmpty(projectTemplates)) {
                projectTemplate = projectTemplates.stream()
                        .filter(i -> i.getName().equals(global.getName()))
                        .collect(Collectors.toList());
            }
            // 如果没有项目级别的模板就创建
            if (CollectionUtils.isEmpty(projectTemplate)) {
                ApiTemplate template = new ApiTemplate();
                BeanUtils.copyBean(template, global);
                template.setId(UUID.randomUUID().toString());
                template.setCreateTime(System.currentTimeMillis());
                template.setUpdateTime(System.currentTimeMillis());
                template.setCreateUser(SessionUtils.getUserId());
                template.setGlobal(false);
                template.setProjectId(customField.getProjectId());
                apiTemplateMapper.insert(template);

                projectService.updateApiTemplate(global.getId(), template.getId(), customField.getProjectId());


                List<CustomFieldTemplate> customFieldTemplate =
                        customFieldTemplateService.getSystemFieldCreateTemplate(customField, global.getId());

                customFieldTemplateService.create(customFieldTemplate, template.getId(),
                        TemplateConstants.FieldTemplateScene.API.name());
            }
        });
        if (CollectionUtils.isNotEmpty(projectTemplates)) {
            customFieldTemplateService.updateProjectTemplateGlobalField(customField,
                    projectTemplates.stream().map(ApiTemplate::getId).collect(Collectors.toList()));
        }
    }

    private void checkExist(ApiTemplate apiTemplate) {
        if (apiTemplate.getName() != null) {
            ApiTemplateExample example = new ApiTemplateExample();
            ApiTemplateExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(apiTemplate.getName())
                    .andProjectIdEqualTo(apiTemplate.getProjectId());
            if (StringUtils.isNotBlank(apiTemplate.getId())) {
                criteria.andIdNotEqualTo(apiTemplate.getId());
            }
            if (apiTemplateMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("template_already") + apiTemplate.getName());
            }
        }
    }

    public ApiTemplate getDefaultTemplate(String projectId) {
        ApiTemplateExample example = new ApiTemplateExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andSystemEqualTo(true);
        List<ApiTemplate> apiTemplates = apiTemplateMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTemplates)) {
            return apiTemplates.get(0);
        }
        example.clear();
        example.createCriteria()
                .andGlobalEqualTo(true);
        return apiTemplateMapper.selectByExample(example).get(0);
    }

    public List<ApiTemplate> getOption(String projectId) {
        List<ApiTemplate> apiTemplates;
        ApiTemplateExample example = new ApiTemplateExample();
        if (StringUtils.isBlank(projectId)) {
            example.createCriteria().andGlobalEqualTo(true)
                    .andSystemEqualTo(true);
            return apiTemplateMapper.selectByExample(example);
        }
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andSystemNotEqualTo(true);
        apiTemplates = apiTemplateMapper.selectByExample(example);
        apiTemplates.add(getDefaultTemplate(projectId));
        return apiTemplates;
    }

    public ApiTemplateDTO getTemplate(String projectId) {
        Project project = projectService.getProjectById(projectId);
        String apiTemplateId = project.getApiTemplateId();
        ApiTemplate apiTemplate;
        ApiTemplateDTO apiTemplateDao = new ApiTemplateDTO();
        if (StringUtils.isNotBlank(apiTemplateId)) {
            apiTemplate = apiTemplateMapper.selectByPrimaryKey(apiTemplateId);
            if (apiTemplate == null) {
                apiTemplate = getDefaultTemplate(projectId);
            }
        } else {
            apiTemplate = getDefaultTemplate(projectId);
        }
        BeanUtils.copyBean(apiTemplateDao, apiTemplate);
        List<CustomFieldDao> result = customFieldService.getCustomFieldByTemplateId(apiTemplate.getId());
        apiTemplateDao.setCustomFields(result);
        return apiTemplateDao;
    }

    public String getLogDetails(String id) {
        ApiTemplate templateWithBLOBs = apiTemplateMapper.selectByPrimaryKey(id);
        if (templateWithBLOBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(templateWithBLOBs, SystemReference.caseFieldColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(templateWithBLOBs.getId()), null, templateWithBLOBs.getName(), templateWithBLOBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
