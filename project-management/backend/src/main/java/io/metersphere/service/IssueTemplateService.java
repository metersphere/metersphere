package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtCustomFieldMapper;
import io.metersphere.base.mapper.ext.ExtCustomFieldTemplateMapper;
import io.metersphere.base.mapper.ext.ExtIssueTemplateMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.ProjectRequest;
import io.metersphere.request.CopyIssueTemplateRequest;
import io.metersphere.request.UpdateIssueTemplateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssueTemplateService extends TemplateBaseService {

    @Resource
    ExtIssueTemplateMapper extIssueTemplateMapper;

    @Resource
    ExtCustomFieldTemplateMapper extCustomFieldTemplateMapper;

    @Resource
    ExtCustomFieldMapper extCustomFieldMapper;

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

    @Resource
    UserGroupMapper userGroupMapper;

    @Resource
    GroupMapper groupMapper;

    @Resource
    GroupService groupService;

    @Resource
    WorkspaceMapper workspaceMapper;

    private static final String CUSTOM_FIELD_TYPE = "select";

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
            // 如果是全局字段，则创建对应项目字段
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
     * 获取该项目下的系统模板
     * - 如果没有，则创建该工项目模板，并关联默认的字段
     * - 如果有，则更新原来关联的 fieldId
     *
     * @param customField
     */
    public void handleSystemFieldCreate(CustomFieldDao customField) {
        IssueTemplateExample example = new IssueTemplateExample();
        example.createCriteria()
                .andGlobalEqualTo(true);
        example.or(example.createCriteria()
                .andProjectIdEqualTo(customField.getProjectId()));
        List<IssueTemplate> issueTemplates = issueTemplateMapper.selectByExampleWithBLOBs(example);

        Map<Boolean, List<IssueTemplate>> templatesMap = issueTemplates.stream()
                .collect(Collectors.groupingBy(IssueTemplate::getGlobal));

        // 获取全局模板
        List<IssueTemplate> globalTemplates = templatesMap.get(true);
        // 获取当前工作空间下模板
        List<IssueTemplate> projectTemplates = templatesMap.get(false);

        globalTemplates.forEach(global -> {
            List<IssueTemplate> projectTemplate = null;
            if (CollectionUtils.isNotEmpty(projectTemplates)) {
                projectTemplate = projectTemplates.stream()
                        .filter(i -> i.getName().equals(global.getName()))
                        .collect(Collectors.toList());
            }
            // 如果没有项目级别的模板就创建
            if (CollectionUtils.isEmpty(projectTemplate)) {
                IssueTemplate template = new IssueTemplate();
                BeanUtils.copyBean(template, global);
                template.setId(UUID.randomUUID().toString());
                template.setCreateTime(System.currentTimeMillis());
                template.setUpdateTime(System.currentTimeMillis());
                template.setCreateUser(SessionUtils.getUserId());
                template.setGlobal(false);
                template.setProjectId(customField.getProjectId());
                issueTemplateMapper.insert(template);

                projectService.updateIssueTemplate(global.getId(), template.getId(), customField.getProjectId());

                List<CustomFieldTemplate> customFieldTemplate =
                        customFieldTemplateService.getSystemFieldCreateTemplate(customField, global.getId());

                customFieldTemplateService.create(customFieldTemplate, template.getId(),
                        TemplateConstants.FieldTemplateScene.ISSUE.name());
            }
        });
        if (CollectionUtils.isNotEmpty(projectTemplates)) {
            customFieldTemplateService.updateProjectTemplateGlobalField(customField,
                    projectTemplates.stream().map(IssueTemplate::getId).collect(Collectors.toList()));
        }
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
                issueTemplate = getDefaultTemplate(projectId);
            }
        } else {
            issueTemplate = getDefaultTemplate(projectId);
        }
        if (!project.getPlatform().equals(issueTemplate.getPlatform())) {
            MSException.throwException("请在项目中配置缺陷模板");
        }
        BeanUtils.copyBean(issueTemplateDao, issueTemplate);
        List<CustomFieldDao> result = customFieldService.getCustomFieldByTemplateId(issueTemplate.getId());
        issueTemplateDao.setCustomFields(result);
        return issueTemplateDao;
    }

    public IssueTemplateCopyDTO getCopyProject(String userId, String workspaceId) {
        IssueTemplateCopyDTO issueTemplateCopyDto = new IssueTemplateCopyDTO();
        // 获取工作空间名称
        Workspace workspace = workspaceMapper.selectByPrimaryKey(workspaceId);
        if (workspace == null) {
            MSException.throwException("workplace id is not exists");
        }
        issueTemplateCopyDto.setWorkspaceName(workspace.getName());
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setWorkspaceId(workspaceId);
        projectRequest.setUserId(userId);
        List<ProjectDTO> userProjectAndGroup = projectService.getUserProject(projectRequest);
        if (userProjectAndGroup.size() == 0) {
            issueTemplateCopyDto.setProjectDTOS(new ArrayList<>());
        }
        Iterator<ProjectDTO> iterator = userProjectAndGroup.iterator();
        while (iterator.hasNext()) {
            ProjectDTO projectDto = iterator.next();
            UserGroupExample example = new UserGroupExample();
            example.createCriteria().andSourceIdEqualTo(projectDto.getId()).andUserIdEqualTo(SessionUtils.getUserId());
            List<UserGroup> userGroups = userGroupMapper.selectByExample(example);
            List<GroupPermissionDTO> groupPermissions = getPermissionsByUserGroups(userGroups);
            boolean isShow = false;
            for (GroupPermissionDTO groupPermission : groupPermissions) {
                GroupResourceDTO projectTemplatePermissions = groupPermission.getPermissions().stream()
                        .filter(permission -> StringUtils.equals(CommonConstants.PROJECT_TEMPLATE, permission.getResource().getId()))
                        .collect(Collectors.toList()).get(0);
                if (projectTemplatePermissions == null) {
                    //模板设置的权限为空
                    continue;
                }
                // 否则校验模板设置中缺陷模板, 自定义字段的权限
                boolean issueTemplateChecked = projectTemplatePermissions.getPermissions().stream()
                        .anyMatch(permission -> permission.getChecked() && StringUtils.equals(PermissionConstants.PROJECT_TEMPLATE_READ_ISSUE_TEMPLATE, permission.getId()));
                if (!issueTemplateChecked) {
                    //模板设置中的缺陷模板权限未勾选
                    continue;
                }
                // 模板设置中的缺陷模板权限已勾选
                isShow = true;
                boolean customChecked = projectTemplatePermissions.getPermissions().stream()
                        .anyMatch(permission -> permission.getChecked() && StringUtils.equals(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM, permission.getId()));
                if (customChecked) {
                    // 模板设置中的自定义字段权限已勾选, 跳出整个用户组权限校验
                    projectDto.setCustomPermissionFlag(Boolean.TRUE);
                    break;
                }
            }

            if (!isShow) {
                iterator.remove();
            }
        }
        issueTemplateCopyDto.setProjectDTOS(userProjectAndGroup);
        return issueTemplateCopyDto;
    }

    public List<IssueTemplate> copy(CopyIssueTemplateRequest request) {
        if (CollectionUtils.isEmpty(request.getTargetProjectIds())) {
            MSException.throwException(Translator.get("target_issue_template_not_checked"));
        }
        if (request.getId() == null) {
            MSException.throwException(Translator.get("source_issue_template_is_empty"));
        }
        List<IssueTemplate> issueTemplateRecords = new ArrayList<>();
        List<CustomField> customFieldRecords = new ArrayList<>();
        List<CustomFieldTemplate> customFieldTemplateRecords = new ArrayList<>();
        // 获取源缺陷模板
        IssueTemplate sourceIssueTemplate = issueTemplateMapper.selectByPrimaryKey(request.getId());
        // 获取源缺陷模板的自定义模板及字段数据
        List<CustomFieldTemplate> sourceCustomFieldTemplates = customFieldTemplateService.getCustomFields(request.getId());
        List<String> fields = sourceCustomFieldTemplates.stream().map(CustomFieldTemplate::getFieldId).collect(Collectors.toList());
        List<CustomField> sourceCustomFields = customFieldService.getFieldByIds(fields);
        request.getTargetProjectIds().forEach(targetProjectId -> {
            IssueTemplate issueTemplateRecord = new IssueTemplate();
            BaseQueryRequest baseQueryRequest = new BaseQueryRequest();
            baseQueryRequest.setProjectId(targetProjectId);
            List<IssueTemplate> targetIssueTemplates = list(baseQueryRequest);
            boolean isExistTargetIssueTemplate = targetIssueTemplates.stream().anyMatch(issueTemplate -> StringUtils.equals(sourceIssueTemplate.getName(), issueTemplate.getName()));
            String recordName;
            if (isExistTargetIssueTemplate) {
                recordName = sourceIssueTemplate.getName().concat("_copy").concat(UUID.randomUUID().toString().substring(0, 8));
            } else {
                recordName = sourceIssueTemplate.getName();
            }
            if (recordName.length() > 64) {
                MSException.throwException(Translator.get("copy_template_name_too_long"));
            }
            BeanUtils.copyBean(issueTemplateRecord, sourceIssueTemplate);
            issueTemplateRecord.setId(UUID.randomUUID().toString());
            issueTemplateRecord.setName(recordName);
            issueTemplateRecord.setCreateTime(System.currentTimeMillis());
            issueTemplateRecord.setUpdateTime(System.currentTimeMillis());
            issueTemplateRecord.setCreateUser(SessionUtils.getUserId());
            issueTemplateRecord.setProjectId(targetProjectId);
            issueTemplateRecord.setGlobal(Boolean.FALSE);
            issueTemplateRecord.setSystem(Boolean.FALSE);
            issueTemplateRecords.add(issueTemplateRecord);
            // 根据复制模式设置自定义字段
            sourceCustomFieldTemplates.forEach(sourceCustomFieldTemplate -> {
                CustomFieldTemplate tarCustomFieldTemplate = new CustomFieldTemplate();
                CustomField tarCustomField = new CustomField();
                CustomField sourceCustomField = sourceCustomFields.stream()
                        .filter(item -> StringUtils.equals(item.getId(), sourceCustomFieldTemplate.getFieldId()))
                        .collect(Collectors.toList()).get(0);
                CustomFieldExample example = new CustomFieldExample();
                example.createCriteria().andNameEqualTo(sourceCustomField.getName())
                        .andSceneEqualTo(sourceCustomField.getScene()).andSystemEqualTo(sourceCustomField.getSystem())
                        .andProjectIdEqualTo(targetProjectId);
                List<CustomField> targetCustomFields = customFieldMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(targetCustomFields)) {
                    BeanUtils.copyBean(tarCustomField, sourceCustomField);
                    // 自定义字段不存在则新增
                    tarCustomField.setId(UUID.randomUUID().toString());
                    tarCustomField.setGlobal(Boolean.FALSE);
                    tarCustomField.setCreateTime(System.currentTimeMillis());
                    tarCustomField.setUpdateTime(System.currentTimeMillis());
                    tarCustomField.setCreateUser(SessionUtils.getUserId());
                    tarCustomField.setProjectId(targetProjectId);
                    customFieldRecords.add(tarCustomField);
                    if (sourceCustomField.getSystem()) {
                        // 系统字段未查到, 则为全局模板Gloal字段
                        CustomFieldExample customFieldExample = new CustomFieldExample();
                        customFieldExample.createCriteria().andNameEqualTo(sourceCustomField.getName())
                                .andSceneEqualTo(sourceCustomField.getScene()).andSystemEqualTo(sourceCustomField.getSystem())
                                .andProjectIdEqualTo("global");
                        List<CustomField> customFields = customFieldMapper.selectByExample(customFieldExample);

                        CustomFieldDao customFieldDao = new CustomFieldDao();
                        BeanUtils.copyBean(customFieldDao, tarCustomField);
                        if (CollectionUtils.isNotEmpty(customFields)) {
                            customFieldDao.setOriginGlobalId(customFields.get(0).getId());
                        }
                        // 新增系统字段, 需处理默认模板
                        handleSystemFieldCreate(customFieldDao);
                    }
                } else {
                    // 否则按照复制模式进行设置
                    BeanUtils.copyBean(tarCustomField, targetCustomFields.get(0));
                    tarCustomField.setCreateTime(System.currentTimeMillis());
                    tarCustomField.setUpdateTime(System.currentTimeMillis());
                    tarCustomField.setCreateUser(SessionUtils.getUserId());
                    if (StringUtils.equals("1", request.getCopyModel())) {
                        // 覆盖模式
                        if (!StringUtils.equals(sourceCustomField.getType(), tarCustomField.getType())) {
                            tarCustomField.setType(sourceCustomField.getType());
                        }
                        tarCustomField.setOptions(sourceCustomField.getOptions());
                        customFieldMapper.updateByPrimaryKeyWithBLOBs(tarCustomField);
                    } else {
                        //追加模式
                        if (sourceCustomField.getSystem()) {
                            // 系统字段
                            String optionsStr;
                            if (StringUtils.equals(CUSTOM_FIELD_TYPE, sourceCustomField.getType())) {
                                // 下拉框选项
                                List<CustomFieldOptionDTO> options = JSON.parseArray(sourceCustomField.getOptions(), CustomFieldOptionDTO.class);
                                options.removeIf(sourceOption -> StringUtils.contains(tarCustomField.getOptions(), sourceOption.getText().toString()));
                                if (CollectionUtils.isNotEmpty(options)) {
                                    optionsStr = JSON.toJSONString(options);
                                    optionsStr = StringUtils.replace(tarCustomField.getOptions(), "]", ",") + StringUtils.replace(optionsStr, "[", StringUtils.EMPTY);
                                } else {
                                    optionsStr = tarCustomField.getOptions();
                                }
                            } else {
                                // 普通值
                                optionsStr = "[]";
                            }
                            tarCustomField.setOptions(optionsStr);
                            customFieldMapper.updateByPrimaryKeyWithBLOBs(tarCustomField);
                        } else {
                            // 非系统字段, 则追加_copy
                            tarCustomField.setId(UUID.randomUUID().toString());
                            tarCustomField.setName(tarCustomField.getName().concat("_copy").concat(UUID.randomUUID().toString().substring(0, 8)));
                            tarCustomField.setOptions(sourceCustomField.getOptions());
                            customFieldRecords.add(tarCustomField);
                        }
                    }
                }
                BeanUtils.copyBean(tarCustomFieldTemplate, sourceCustomFieldTemplate);
                tarCustomFieldTemplate.setId(UUID.randomUUID().toString());
                tarCustomFieldTemplate.setFieldId(tarCustomField.getId());
                tarCustomFieldTemplate.setTemplateId(issueTemplateRecord.getId());
                customFieldTemplateRecords.add(tarCustomFieldTemplate);
            });
        });
        // 批量插入目标项目缺陷模板
        if (CollectionUtils.isNotEmpty(issueTemplateRecords)) {
            extIssueTemplateMapper.batchInsert(issueTemplateRecords);
        }
        // 批量插入自定义字段数据
        if (CollectionUtils.isNotEmpty(customFieldTemplateRecords)) {
            extCustomFieldTemplateMapper.batchInsert(customFieldTemplateRecords);
        }
        // 批量插入自定义字段模板数据
        if (CollectionUtils.isNotEmpty(customFieldRecords)) {
            extCustomFieldMapper.batchInsert(customFieldRecords);
        }
        return issueTemplateRecords;
    }

    public String getLogDetails(String id, List<CustomFieldTemplate> newFields) {
        List<DetailColumn> columns = new LinkedList<>();
        IssueTemplate templateWithBLOBs = issueTemplateMapper.selectByPrimaryKey(id);
        if (templateWithBLOBs == null) {
            return null;
        }
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria().andTemplateIdEqualTo(templateWithBLOBs.getId());
        example.createCriteria().andSceneEqualTo("ISSUE");
        List<CustomFieldTemplate> oldFields = customFieldTemplateMapper.selectByExample(example);
        Collections.sort(oldFields, (f1, f2) -> f1.getKey().compareTo(f2.getKey()));
        if (newFields.size() > oldFields.size()) {
            int size = newFields.size() - oldFields.size();
            for (int i = 0; i < size; i++) {
                CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
                oldFields.add(oldFields.size(), customFieldTemplate);
            }
        }
        return getCustomFieldColums(columns, templateWithBLOBs, oldFields);
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
        Collections.sort(customFieldTemplates, (f1, f2) -> f1.getKey().compareTo(f2.getKey()));
        return getCustomFieldColums(columns, templateWithBLOBs, customFieldTemplates);
    }

    private String getCustomFieldColums(List<DetailColumn> columns, IssueTemplate templateWithBLOBs, List<CustomFieldTemplate> customFields) {
        for (CustomFieldTemplate customFieldTemplate : customFields) {
            CustomField customField = customFieldMapper.selectByPrimaryKey(customFieldTemplate.getFieldId());
            CustomFieldDao customFieldDao = new CustomFieldDao();
            if (customField != null) {
                BeanUtils.copyBean(customFieldDao, customField);
                customFieldDao.setDefaultValue(customFieldTemplate.getDefaultValue());
                List<DetailColumn> columnsField = ReflexObjectUtil.getColumns(customFieldDao, SystemReference.issueFieldColumns);
                columns.addAll(columnsField);
            } else {
                customFieldDao.setName(StringUtils.EMPTY);
                customFieldDao.setScene(StringUtils.EMPTY);
                customFieldDao.setType(StringUtils.EMPTY);
                customFieldDao.setSystem(null);
                customFieldDao.setRemark(StringUtils.EMPTY);
                customFieldDao.setDefaultValue(StringUtils.EMPTY);
                List<DetailColumn> columnsField = ReflexObjectUtil.getColumns(customFieldDao, SystemReference.issueFieldColumns);
                columns.addAll(columnsField);
            }
        }
        OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(templateWithBLOBs.getId()),
                templateWithBLOBs.getProjectId(), templateWithBLOBs.getName(), templateWithBLOBs.getCreateUser(), columns);
        return JSON.toJSONString(details);
    }

    private List<GroupPermissionDTO> getPermissionsByUserGroups(List<UserGroup> userGroups) {
        List<GroupPermissionDTO> permissionsByUserGroups = new ArrayList<>();
        userGroups.forEach(userGroup -> {
            Group group = groupMapper.selectByPrimaryKey(userGroup.getGroupId());
            GroupPermissionDTO groupResource = groupService.getGroupResource(group);
            permissionsByUserGroups.add(groupResource);
        });
        return permissionsByUserGroups;
    }

    @MsAuditLog(module = OperLogModule.PROJECT_TEMPLATE_MANAGEMENT, type = OperLogConstants.COPY, content = "#msClass.getLogDetails(#targetProjectId, #targetProjectName)", msClass = IssueTemplateService.class)
    public void copyIssueTemplateLog(String targetProjectId, String targetProjectName) {
    }

    public String getLogDetails(String targetProjectId, String targetProjectName) {
        if (targetProjectId == null) {
            return null;
        }
        OperatingLogDetails details = new OperatingLogDetails(targetProjectId, targetProjectId, targetProjectName, null, null);
        return JSON.toJSONString(details);
    }

    public String getLogDetails(String issueTemplateId) {
        if (StringUtils.isEmpty(issueTemplateId)) {
            return null;
        }
        IssueTemplate issueTemplate = issueTemplateMapper.selectByPrimaryKey(issueTemplateId);
        if (issueTemplate == null) {
            return null;
        }
        OperatingLogDetails details = new OperatingLogDetails(issueTemplateId, issueTemplate.getProjectId(), issueTemplate.getName(), issueTemplate.getCreateUser(), null);
        return JSON.toJSONString(details);
    }
}
