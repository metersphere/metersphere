package io.metersphere.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ext.CustomFieldResource;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.ext.ExtBaseMapper;
import io.metersphere.base.mapper.ext.ExtCustomFieldResourceMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.customfield.CustomFieldResourceRequest;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.track.dto.CustomFieldResourceCompatibleDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldResourceService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Lazy
    @Resource
    ProjectService projectService;

    @Lazy
    @Resource
    WorkspaceService workspaceService;

    @Resource
    ExtTestCaseMapper extTestCaseMapper;

    @Resource
    ExtIssuesMapper extIssuesMapper;

    @Resource
    CustomFieldService customFieldService;

    @Resource
    ExtBaseMapper extBaseMapper;

    @Resource
    ExtCustomFieldResourceMapper extCustomFieldResourceMapper;

    @Lazy
    @Resource
    SystemParameterService systemParameterService;

    private int initCount = 0;

    protected void addFields(String tableName, String resourceId, List<CustomFieldResource> addFields) {
        if (CollectionUtils.isNotEmpty(addFields)) {
            this.checkInit();
            addFields.forEach(field -> {
                field.setResourceId(resourceId);
                extCustomFieldResourceMapper.insert(tableName, field);
            });
        }
    }

    protected void editFields(String tableName, String resourceId, List<CustomFieldResource> editFields) {
        if (CollectionUtils.isNotEmpty(editFields)) {
            this.checkInit();
            editFields.forEach(field -> {
                field.setResourceId(resourceId);
                extCustomFieldResourceMapper.updateByPrimaryKeySelective(tableName, field);
            });
        }
    }

    protected int updateByPrimaryKeySelective(String tableName, CustomFieldResource field) {
        return extCustomFieldResourceMapper.updateByPrimaryKeySelective(tableName, field);
    }

    protected int insert(String tableName, CustomFieldResource field) {
        return extCustomFieldResourceMapper.insert(tableName, field);
    }

    protected List<CustomFieldResource> getByResourceId(String tableName, String resourceId) {
        return extCustomFieldResourceMapper.getByResourceId(tableName, resourceId);
    }

    protected List<CustomFieldResource>  getByResourceIds(String tableName, List<String> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new ArrayList<>();
        }
        return extCustomFieldResourceMapper.getByResourceIds(tableName, resourceIds);
    }

    protected Map<String, List<CustomFieldDao>> getMapByResourceIds(String tableName, List<String> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new HashMap<>();
        }
        List<CustomFieldResource> customFieldResources = getByResourceIds(tableName, resourceIds);
        Map<String, List<CustomFieldDao>> fieldMap = new HashMap<>();
        customFieldResources.forEach(i -> {
            List<CustomFieldDao> fields = fieldMap.get(i.getResourceId());
            if (fields == null) {
                fields = new ArrayList<>();
            }
            CustomFieldDao customFieldDao = new CustomFieldDao();
            customFieldDao.setId(i.getFieldId());
            customFieldDao.setValue(i.getValue());
            customFieldDao.setTextValue(i.getTextValue());
            fields.add(customFieldDao);
            fieldMap.put(i.getResourceId(), fields);
        });
        return fieldMap;
    }

    protected void deleteByResourceId(String tableName, String resourceId) {
        extCustomFieldResourceMapper.deleteByResourceId(tableName, resourceId);
    }

    protected void deleteByResourceIds(String tableName, List<String> resourceIds) {
        extCustomFieldResourceMapper.deleteByResourceIds(tableName, resourceIds);
    }

    protected void checkInit() {
        String value = systemParameterService.getValue("init.custom.field.resource");
        if (StringUtils.isNotBlank(value) && value.equals("over")) {
            return;
        }
        MSException.throwException("数据升级处理中，请稍后重试！");
    }

    /**
     * 初始化数据
     * 兼容旧数据
     */
    public void compatibleData() {
        LogUtil.info("init CustomFieldResourceService compatibleData start ===================");
        List<String> projectIds = projectService.getProjectIds();
        Map<String, CustomField> globalFieldMap = customFieldService.getGlobalNameMapByProjectId();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtCustomFieldResourceMapper batchMapper = sqlSession.getMapper(ExtCustomFieldResourceMapper.class);
        CustomFieldMapper customFieldBatchMapper = sqlSession.getMapper(CustomFieldMapper.class);

        try {
            CustomFieldResourceRequest param = new CustomFieldResourceRequest();
            param.setGlobalFieldMap(globalFieldMap);
            param.setSqlSession(sqlSession);
            param.setBatchMapper(batchMapper);
            param.setCustomFieldBatchMapper(customFieldBatchMapper);
            projectIds.forEach(projectId -> {
                param.setWdFieldMap(customFieldService.getNameMapByProjectId(projectId));
                param.setJiraSyncFieldMap(new HashMap<>());
                param.setProjectId(projectId);
                compatibleTestCase(param);
                Project project = projectService.getProjectById(projectId);
                compatibleIssue(param, project);
            });
            sqlSession.flushStatements();
            sqlSession.commit();
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
        LogUtil.info("init CustomFieldResourceService compatibleData end ===================");
    }

    public void compatibleTestCase(CustomFieldResourceRequest param) {
        param.setEnableJiraSync(false);
        param.setResourceType(TemplateConstants.FieldTemplateScene.TEST_CASE.name());
        this.compatibleCommon(param);
    }

    public void compatibleIssue( CustomFieldResourceRequest param, Project project) {
        // 是否勾选了自动获取模板
        boolean enableJiraSync = project.getPlatform().equals(IssuesManagePlatform.Jira.toString()) &&
                (project.getThirdPartTemplate() == null ? false : project.getThirdPartTemplate() );
        param.setEnableJiraSync(enableJiraSync);
        param.setResourceType(TemplateConstants.FieldTemplateScene.ISSUE.name());
       this.compatibleCommon(param);
    }

    /**
     * 对旧数据做数据初始化
     * 自定义字段不再存储在对应表的 customFields 字段
     * 改为添加中间表
     */
    protected void compatibleCommon(CustomFieldResourceRequest param) {
        int pageNum = 1;
        int pageSize = 1000;
        List<CustomFieldResourceCompatibleDTO> list;
        do {
            if (param.getResourceType().equals(TemplateConstants.FieldTemplateScene.ISSUE.name())) {
                list = extIssuesMapper.getForCompatibleCustomField(param.getProjectId(), (pageNum - 1) * pageSize, pageSize);
            } else {
                list = extTestCaseMapper.getForCompatibleCustomField(param.getProjectId(), (pageNum - 1) * pageSize, pageSize);
            }
            pageNum++;
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(resource -> {
                    // 获取对应资源的自定义字段，存入 CustomFieldResource
                    String customFields = resource.getCustomFields();
                    if (StringUtils.isNotBlank(customFields)) {
                        try {
                            List<CustomFieldItemDTO> fields = CustomFieldService.getCustomFields(customFields);
                            Set<String> fieldSet = new HashSet<>();
                            fields.forEach(field -> {
                                try {
                                    CustomField customField;
                                    if (StringUtils.isBlank(field.getName())) { return; }
                                    if (param.isEnableJiraSync()) {
                                        if (StringUtils.isBlank(field.getId()) || field.getId().length() == 36) {
                                            // 自定义字段中id为空，或者是uuid的，就不处理了
                                            return;
                                        }
                                        Map<String, CustomField> jiraSyncFieldMap = param.getJiraSyncFieldMap();
                                        if (jiraSyncFieldMap.containsKey(field.getId())) {
                                            customField = jiraSyncFieldMap.get(field.getId());
                                        } else {
                                            customField = createCustomField(param.getProjectId(), field, param);
                                            param.getJiraSyncFieldMap().put(field.getId(), customField);
                                        }
                                    } else {
                                        customField = Optional.ofNullable(param.getWdFieldMap().get(field.getName() + param.getResourceType()))
                                                .orElse(param.getGlobalFieldMap().get(field.getName() + param.getResourceType()));

                                    }
                                    createCustomFieldResource(customField, fieldSet, resource, field, param);
                                } catch (Exception e) {
                                    LogUtil.error(e);
                                }
                            });
                        } catch (Exception e) {
                            LogUtil.error(e);
                        }
                    }
                });
            }
        } while (list.size() == pageSize);
    }

    /**
     * 如果是jira勾选了自动获取模板
     * 则创建对应的自定义字段，并标记成 thirdPart 为 true
     * @param projectId
     * @param field
     * @param param
     * @return
     */
    private CustomField createCustomField(String projectId, CustomFieldItemDTO field, CustomFieldResourceRequest param) {
        CustomField customField = new CustomField();
        customField.setId(UUID.randomUUID().toString());
        customField.setUpdateTime(System.currentTimeMillis());
        customField.setCreateTime(System.currentTimeMillis());
        customField.setGlobal(false);
        customField.setSystem(false);
        customField.setName(field.getId());
        customField.setScene(TemplateConstants.FieldTemplateScene.ISSUE.name());
        customField.setThirdPart(true);
        customField.setType(field.getType());
        customField.setProjectId(projectId);
        CustomFieldMapper customFieldBatchMapper = param.getCustomFieldBatchMapper();
        customFieldBatchMapper.insert(customField);
        initCount++;
        if (initCount > 1000) {
            param.getSqlSession().flushStatements();
            param.getSqlSession().commit();
            initCount = 0;
        }
        return customField;
    }

    private void createCustomFieldResource(CustomField customField, Set<String> fieldSet, CustomFieldResourceCompatibleDTO resource,
                                           CustomFieldItemDTO field, CustomFieldResourceRequest param) {
        if (customField != null && field.getValue() != null) {
            if (fieldSet.contains(customField.getId())) {
                return;
            }
            String tableName;
            if (param.getResourceType().equals(TemplateConstants.FieldTemplateScene.ISSUE.name())) {
                tableName = "custom_field_issues";
            } else {
                tableName = "custom_field_test_case";
            }
            CustomFieldResource customFieldResource = new CustomFieldResource();
            customFieldResource.setResourceId(resource.getId());
            customFieldResource.setFieldId(customField.getId());
            if (StringUtils.isNotBlank(customField.getType())
                    && StringUtils.equalsAny(CustomFieldType.RICH_TEXT.getValue(), CustomFieldType.TEXTAREA.getValue())) {
                customFieldResource.setTextValue(field.getValue().toString());
                param.getBatchMapper().insert(tableName, customFieldResource);
            } else {
                if (field.getValue().toString().length() < 490) {
                    customFieldResource.setValue(JSONObject.toJSONString(field.getValue()));
                    param.getBatchMapper().insert(tableName, customFieldResource);
                }
            }
            fieldSet.add(customField.getId());
            initCount++;
            if (initCount > 1000) {
                param.getSqlSession().flushStatements();
                param.getSqlSession().commit();
                initCount = 0;
            }
        }
    }
}
