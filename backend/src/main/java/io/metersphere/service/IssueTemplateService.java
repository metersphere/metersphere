package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
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
            projectService.updateIssueTemplate(originId, id);
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
     * @param customField
     */
    public void handleSystemFieldCreate(CustomField customField) {
        IssueTemplate workspaceSystemTemplate = getWorkspaceSystemTemplate(customField.getWorkspaceId());
        if (workspaceSystemTemplate == null) {
            createTemplateWithUpdateField(customField.getWorkspaceId(), customField);
        } else {
            updateRelateWithUpdateField(workspaceSystemTemplate, customField);
        }
    }

    private IssueTemplate getWorkspaceSystemTemplate(String workspaceId) {
        IssueTemplateExample example = new IssueTemplateExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(workspaceId)
                .andSystemEqualTo(true);
        List<IssueTemplate> issueTemplates = issueTemplateMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(issueTemplates)) {
            return issueTemplates.get(0);
        }
        return null;
    }

    private void createTemplateWithUpdateField(String workspaceId, CustomField customField) {
        UpdateIssueTemplateRequest request = new UpdateIssueTemplateRequest();
        IssueTemplate issueTemplate = new IssueTemplate();
        issueTemplate.setName("default");
        issueTemplate.setPlatform(TemplateConstants.IssueTemplatePlatform.metersphere.name());
        issueTemplate.setGlobal(false);
        issueTemplate.setSystem(true);
        issueTemplate.setWorkspaceId(workspaceId);
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
                    .andWorkspaceIdEqualTo(issueTemplate.getWorkspaceId());
            if (StringUtils.isNotBlank(issueTemplate.getId())) {
                criteria.andIdNotEqualTo(issueTemplate.getId());
            }
            if (issueTemplateMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("template_already") + issueTemplate.getName());
            }
        }
    }

    public List<IssueTemplate> getSystemTemplates(String workspaceId) {
        IssueTemplateExample example = new IssueTemplateExample();
        example.createCriteria().andWorkspaceIdEqualTo(workspaceId)
                .andSystemEqualTo(true);
        example.or(example.createCriteria().andGlobalEqualTo(true));
        List<IssueTemplate> issueTemplates = issueTemplateMapper.selectByExample(example);
        Iterator<IssueTemplate> iterator = issueTemplates.iterator();
        while (iterator.hasNext()) {
            IssueTemplate next = iterator.next();
            for (IssueTemplate item: issueTemplates) {
                if (next.getGlobal() && !item.getGlobal() && StringUtils.equals(item.getName(), next.getName())) {
                    // 如果有工作空间的模板则过滤掉全局模板
                    iterator.remove();
                    break;
                }
            }
        }
        return issueTemplates;
    }

    public IssueTemplate getDefaultTemplate(String workspaceId) {
        IssueTemplateExample example = new IssueTemplateExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(workspaceId)
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

    public List<IssueTemplate> getOption(String workspaceId) {
        IssueTemplateExample example = new IssueTemplateExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(workspaceId)
                .andSystemEqualTo(false);
        List<IssueTemplate> issueTemplates = issueTemplateMapper.selectByExample(example);
        issueTemplates.addAll(getSystemTemplates(workspaceId));
        return issueTemplates;
    }

    public IssueTemplateDao getTemplate(String projectId) {
        Project project = projectService.getProjectById(projectId);
        String issueTemplateId = project.getIssueTemplateId();
        IssueTemplate issueTemplate = null;
        IssueTemplateDao issueTemplateDao = new IssueTemplateDao();
        if (StringUtils.isNotBlank(issueTemplateId)) {
            issueTemplate = issueTemplateMapper.selectByPrimaryKey(issueTemplateId);
            if (issueTemplate == null) {
                issueTemplate = getDefaultTemplate(project.getWorkspaceId());
            }
        } else {
            issueTemplate = getDefaultTemplate(project.getWorkspaceId());
        }
        BeanUtils.copyBean(issueTemplateDao, issueTemplate);
        List<CustomFieldDao> result = customFieldService.getCustomFieldByTemplateId(issueTemplate.getId());
        issueTemplateDao.setCustomFields(result);
        return issueTemplateDao;
    }

    public String getLogDetails(String id) {
        IssueTemplate templateWithBLOBs = issueTemplateMapper.selectByPrimaryKey(id);
        if (templateWithBLOBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(templateWithBLOBs, SystemReference.issueFieldColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(templateWithBLOBs.getId()), null, templateWithBLOBs.getName(), templateWithBLOBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
