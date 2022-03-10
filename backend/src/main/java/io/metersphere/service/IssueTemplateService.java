package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.CustomFieldTemplateMapper;
import io.metersphere.base.mapper.IssueTemplateMapper;
import io.metersphere.base.mapper.ext.ExtIssueTemplateMapper;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.UpdateIssueTemplateRequest;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.IssueTemplateDao;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssueTemplateService extends TemplateBaseService {

    @Resource
    ExtIssueTemplateMapper extIssueTemplateMapper;

    @Resource
    IssueTemplateMapper issueTemplateMapper;

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    @Resource
    CustomFieldService customFieldService;

    @Resource
    ProjectService projectService;

    @Resource
    CustomFieldMapper customFieldMapper;

    @Resource
    CustomFieldTemplateMapper customFieldTemplateMapper;

    public String add(UpdateIssueTemplateRequest request) {
        checkExist(request);
        IssueTemplate template = new IssueTemplate();
        BeanUtils.copyBean(template, request);
        template.setId(UUID.randomUUID().toString());
        template.setCreateTime(System.currentTimeMillis());
        template.setUpdateTime(System.currentTimeMillis());
        template.setCreateUser(SessionUtils.getUserId());
        if (template.getSystem() == null) {
            template.setSystem(false);
        }
        request.setId(template.getId());
        template.setGlobal(false);
        issueTemplateMapper.insert(template);
        customFieldTemplateService.create(request.getCustomFields(), template.getId(),
                TemplateConstants.FieldTemplateScene.ISSUE.name());
        return template.getId();
    }

    public List<IssueTemplate> list(BaseQueryRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extIssueTemplateMapper.list(request);
    }

    public void delete(String id) {
        checkTemplateUsed(id, projectService::getByIssueTemplateId);
        issueTemplateMapper.deleteByPrimaryKey(id);
        customFieldTemplateService.deleteByTemplateId(id);
    }

    public void update(UpdateIssueTemplateRequest request) {
        if (request.getGlobal() != null && request.getGlobal()) {
            String originId = request.getId();
            // 如果是全局字段，则创建对应工作空间字段
            String id = add(request);
            projectService.updateIssueTemplate(originId, id, request.getProjectId());
        } else {
            checkExist(request);
            customFieldTemplateService.deleteByTemplateId(request.getId());
            IssueTemplate template = new IssueTemplate();
            BeanUtils.copyBean(template, request);
            template.setUpdateTime(System.currentTimeMillis());
            issueTemplateMapper.updateByPrimaryKeySelective(template);
            customFieldTemplateService.create(request.getCustomFields(), request.getId(),
                    TemplateConstants.FieldTemplateScene.ISSUE.name());
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
        IssueTemplate workspaceSystemTemplate = getWorkspaceSystemTemplate(customField.getProjectId());
        if (workspaceSystemTemplate == null) {
            createTemplateWithUpdateField(customField.getProjectId(), customField);
        } else {
            updateRelateWithUpdateField(workspaceSystemTemplate, customField);
        }
    }

    private IssueTemplate getWorkspaceSystemTemplate(String workspaceId) {
        IssueTemplateExample example = new IssueTemplateExample();
        example.createCriteria()
                .andProjectIdEqualTo(workspaceId)
                .andSystemEqualTo(true);
        List<IssueTemplate> issueTemplates = issueTemplateMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(issueTemplates)) {
            return issueTemplates.get(0);
        }
        return null;
    }

    private void createTemplateWithUpdateField(String projectId, CustomField customField) {
        UpdateIssueTemplateRequest request = new UpdateIssueTemplateRequest();
        IssueTemplate issueTemplate = new IssueTemplate();
        issueTemplate.setName("default");
        issueTemplate.setPlatform(TemplateConstants.IssueTemplatePlatform.metersphere.name());
        issueTemplate.setGlobal(false);
        issueTemplate.setSystem(true);
        issueTemplate.setProjectId(projectId);
        BeanUtils.copyBean(request, issueTemplate);
        List<CustomFieldTemplate> systemFieldCreateTemplate =
                customFieldTemplateService.getSystemFieldCreateTemplate(customField, TemplateConstants.FieldTemplateScene.ISSUE.name());
        request.setCustomFields(systemFieldCreateTemplate);
        add(request);
    }

    private void updateRelateWithUpdateField(IssueTemplate template, CustomField customField) {
        CustomField globalField = customFieldService.getGlobalFieldByName(customField.getName());
        customFieldTemplateService.updateFieldIdByTemplate(template.getId(), globalField.getId(), customField.getId());
    }

    private void checkExist(IssueTemplate issueTemplate) {
        if (issueTemplate.getName() != null) {
            IssueTemplateExample example = new IssueTemplateExample();
            IssueTemplateExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(issueTemplate.getName())
                    .andProjectIdEqualTo(issueTemplate.getProjectId());
            if (StringUtils.isNotBlank(issueTemplate.getId())) {
                criteria.andIdNotEqualTo(issueTemplate.getId());
            }
            if (issueTemplateMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("template_already") + issueTemplate.getName());
            }
        }
    }

    public List<IssueTemplate> getSystemTemplates(String projectId) {
        IssueTemplateExample example = new IssueTemplateExample();
        example.createCriteria().andProjectIdEqualTo(projectId)
                .andSystemEqualTo(true);
        example.or(example.createCriteria().andGlobalEqualTo(true));
        List<IssueTemplate> issueTemplates = issueTemplateMapper.selectByExample(example);
        Iterator<IssueTemplate> iterator = issueTemplates.iterator();
        while (iterator.hasNext()) {
            IssueTemplate next = iterator.next();
            for (IssueTemplate item : issueTemplates) {
                if (next.getGlobal() && !item.getGlobal() && StringUtils.equals(item.getName(), next.getName())) {
                    // 如果有工作空间的模板则过滤掉全局模板
                    iterator.remove();
                    break;
                }
            }
        }
        return issueTemplates;
    }

    public IssueTemplate getDefaultTemplate(String projectId) {
        IssueTemplateExample example = new IssueTemplateExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andSystemEqualTo(true);
        List<IssueTemplate> issueTemplates = issueTemplateMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(issueTemplates)) {
            return issueTemplates.get(0);
        } else {
            example.clear();
            example.createCriteria()
                    .andGlobalEqualTo(true);
            return issueTemplateMapper.selectByExample(example).get(0);
        }
    }

    public List<IssueTemplate> getOption(String projectId) {
        List<IssueTemplate> issueTemplates;
        IssueTemplateExample example = new IssueTemplateExample();
        if (StringUtils.isBlank(projectId)) {
            example.createCriteria().andGlobalEqualTo(true).andSystemEqualTo(true);
            return issueTemplateMapper.selectByExample(example);
        }
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andSystemEqualTo(false);
        issueTemplates = issueTemplateMapper.selectByExample(example);
        issueTemplates.addAll(getSystemTemplates(projectId));
        return issueTemplates;
    }

    public IssueTemplateDao getTemplate(String projectId) {
        Project project = projectService.getProjectById(projectId);
        String issueTemplateId = project.getIssueTemplateId();
        IssueTemplate issueTemplate;
        IssueTemplateDao issueTemplateDao = new IssueTemplateDao();
        if (StringUtils.isNotBlank(issueTemplateId)) {
            issueTemplate = issueTemplateMapper.selectByPrimaryKey(issueTemplateId);
            if (issueTemplate == null) {
                issueTemplate = getDefaultTemplate(project.getWorkspaceId());
            }
        } else {
            issueTemplate = getDefaultTemplate(project.getWorkspaceId());
        }
        if (!project.getPlatform().equals(issueTemplate.getPlatform())) {
            MSException.throwException("请在项目中配置缺陷模板");
        }
        BeanUtils.copyBean(issueTemplateDao, issueTemplate);
        List<CustomFieldDao> result = customFieldService.getCustomFieldByTemplateId(issueTemplate.getId());
        issueTemplateDao.setCustomFields(result);
        return issueTemplateDao;
    }

    public String getLogDetails(String id, List<CustomFieldTemplate> newCustomFieldTemplates) {
        List<DetailColumn> columns = new LinkedList<>();
        IssueTemplate templateWithBLOBs = issueTemplateMapper.selectByPrimaryKey(id);
        if (templateWithBLOBs == null) {
            return null;
        }
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria().andTemplateIdEqualTo(templateWithBLOBs.getId());
        example.createCriteria().andSceneEqualTo("ISSUE");
        List<CustomFieldTemplate> customFieldTemplates = customFieldTemplateMapper.selectByExample(example);
        if (newCustomFieldTemplates.size() > customFieldTemplates.size()) {
            for (int i = 0; i < newCustomFieldTemplates.size() - customFieldTemplates.size(); i++) {
                CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
                customFieldTemplates.add(customFieldTemplate);
            }
        }

        return getCustomFieldColums(columns, templateWithBLOBs, customFieldTemplates);

    }

    public String getLogDetails(UpdateIssueTemplateRequest request) {
        List<DetailColumn> columns = new LinkedList<>();
        IssueTemplate templateWithBLOBs = issueTemplateMapper.selectByPrimaryKey(request.getId());
        if (templateWithBLOBs == null) {
            return null;
        }
        List<CustomFieldTemplate> newCustomFieldTemplates = request.getCustomFields();
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria().andTemplateIdEqualTo(templateWithBLOBs.getId());
        example.createCriteria().andSceneEqualTo("ISSUE");
        List<CustomFieldTemplate> customFieldTemplates = customFieldTemplateMapper.selectByExample(example);
        if (newCustomFieldTemplates.size() < customFieldTemplates.size()) {
            for (int i = 0; i < customFieldTemplates.size() - newCustomFieldTemplates.size(); i++) {
                CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
                newCustomFieldTemplates.add(customFieldTemplate);
            }
        }
        return getCustomFieldColums(columns, templateWithBLOBs, newCustomFieldTemplates);
    }

    private String getCustomFieldColums(List<DetailColumn> columns, IssueTemplate templateWithBLOBs, List<CustomFieldTemplate> customFields) {
        for (CustomFieldTemplate customFieldTemplate : customFields) {
            CustomField customField = customFieldMapper.selectByPrimaryKey(customFieldTemplate.getFieldId());
            CustomFieldDao customFieldDao = new CustomFieldDao();
            BeanUtils.copyBean(customFieldDao, customField);
            customFieldDao.setDefaultValue(customFieldTemplate.getDefaultValue());
            List<DetailColumn> columnsField = ReflexObjectUtil.getColumns(customFieldDao, SystemReference.issueFieldColumns);
            columns.addAll(columnsField);
        }
        List<DetailColumn> columnIssues = ReflexObjectUtil.getColumns(templateWithBLOBs, SystemReference.issueFieldColumns);
        columns.addAll(columnIssues);
        OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(templateWithBLOBs.getId()), null, templateWithBLOBs.getName(), templateWithBLOBs.getCreateUser(), columns);
        return JSON.toJSONString(details);
    }
}
