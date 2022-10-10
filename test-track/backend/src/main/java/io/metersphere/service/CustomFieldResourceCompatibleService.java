package io.metersphere.service;

import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.ext.BaseCustomFieldResourceMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.dto.CustomFieldResourceCompatibleDTO;
import io.metersphere.dto.CustomFieldResourceDTO;
import io.metersphere.request.template.CustomFieldResourceRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class CustomFieldResourceCompatibleService {
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtIssuesMapper extIssuesMapper;
    @Resource
    private ExtTestCaseMapper extTestCaseMapper;

    private int initCount = 0;

    /**
     * 初始化数据
     * 兼容旧数据
     */
    public void compatibleData() {
        LogUtil.info("init CustomFieldResourceService compatibleData start ===================");
        List<String> projectIds = baseProjectService.getProjectIds();
        Map<String, CustomField> globalFieldMap = baseCustomFieldService.getGlobalNameMapByProjectId();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        BaseCustomFieldResourceMapper batchMapper = sqlSession.getMapper(BaseCustomFieldResourceMapper.class);
        CustomFieldMapper customFieldBatchMapper = sqlSession.getMapper(CustomFieldMapper.class);

        try {
            CustomFieldResourceRequest param = new CustomFieldResourceRequest();
            param.setGlobalFieldMap(globalFieldMap);
            param.setSqlSession(sqlSession);
            param.setBatchMapper(batchMapper);
            param.setCustomFieldBatchMapper(customFieldBatchMapper);
            projectIds.forEach(projectId -> {
                param.setWdFieldMap(baseCustomFieldService.getNameMapByProjectId(projectId));
                param.setJiraSyncFieldMap(new HashMap<>());
                param.setProjectId(projectId);
                compatibleTestCase(param);
                Project project = baseProjectService.getProjectById(projectId);
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

    public void compatibleIssue(CustomFieldResourceRequest param, Project project) {
        // 是否勾选了自动获取模板
        boolean enableJiraSync = project.getPlatform().equals(IssuesManagePlatform.Jira.toString()) &&
                (project.getThirdPartTemplate() == null ? false : project.getThirdPartTemplate());
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
                            List<CustomFieldItemDTO> fields = baseCustomFieldService.getCustomFields(customFields);
                            Set<String> fieldSet = new HashSet<>();
                            fields.forEach(field -> {
                                try {
                                    CustomField customField;
                                    if (StringUtils.isBlank(field.getName())) {
                                        return;
                                    }
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
     *
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
            CustomFieldResourceDTO customFieldResource = new CustomFieldResourceDTO();
            customFieldResource.setResourceId(resource.getId());
            customFieldResource.setFieldId(customField.getId());
            if (StringUtils.isNotBlank(customField.getType())
                    && StringUtils.equalsAny(CustomFieldType.RICH_TEXT.getValue(), CustomFieldType.TEXTAREA.getValue())) {
                customFieldResource.setTextValue(field.getValue().toString());
                param.getBatchMapper().insert(tableName, customFieldResource);
            } else {
                if (field.getValue().toString().length() < 490) {
                    customFieldResource.setValue(JSON.toJSONString(field.getValue()));
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
