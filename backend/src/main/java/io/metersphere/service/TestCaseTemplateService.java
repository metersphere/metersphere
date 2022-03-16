package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseTemplateMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseTemplateMapper;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.UpdateCaseFieldTemplateRequest;
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
public class TestCaseTemplateService extends TemplateBaseService {

    @Resource
    ExtTestCaseTemplateMapper extTestCaseTemplateMapper;

    @Resource
    TestCaseTemplateMapper testCaseTemplateMapper;

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    @Resource
    CustomFieldService customFieldService;

    @Resource
    ProjectService projectService;

    public String add(UpdateCaseFieldTemplateRequest request) {
        checkExist(request);
        TestCaseTemplateWithBLOBs testCaseTemplate = new TestCaseTemplateWithBLOBs();
        BeanUtils.copyBean(testCaseTemplate, request);
        testCaseTemplate.setId(UUID.randomUUID().toString());
        testCaseTemplate.setCreateTime(System.currentTimeMillis());
        testCaseTemplate.setUpdateTime(System.currentTimeMillis());
        testCaseTemplate.setGlobal(false);
        testCaseTemplate.setCreateUser(SessionUtils.getUserId());
        if (testCaseTemplate.getSystem() == null) {
            testCaseTemplate.setSystem(false);
        }
        request.setId(testCaseTemplate.getId());
        testCaseTemplateMapper.insert(testCaseTemplate);
        customFieldTemplateService.create(request.getCustomFields(), testCaseTemplate.getId(),
                TemplateConstants.FieldTemplateScene.TEST_CASE.name());
        return testCaseTemplate.getId();
    }

    public List<TestCaseTemplateWithBLOBs> list(BaseQueryRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extTestCaseTemplateMapper.list(request);
    }

    public void delete(String id) {
        checkTemplateUsed(id, projectService::getByCaseTemplateId);
        testCaseTemplateMapper.deleteByPrimaryKey(id);
        customFieldTemplateService.deleteByTemplateId(id);
    }

    public void update(UpdateCaseFieldTemplateRequest request) {
        if (request.getGlobal() != null && request.getGlobal()) {
            String originId = request.getId();
            // 如果是全局字段，则创建对应工作空间字段
            String id = add(request);
            projectService.updateCaseTemplate(originId, id, request.getProjectId());
        } else {
            checkExist(request);
            customFieldTemplateService.deleteByTemplateId(request.getId());
            TestCaseTemplateWithBLOBs TestCaseTemplate = new TestCaseTemplateWithBLOBs();
            BeanUtils.copyBean(TestCaseTemplate, request);
            TestCaseTemplate.setUpdateTime(System.currentTimeMillis());
            testCaseTemplateMapper.updateByPrimaryKeySelective(TestCaseTemplate);
            customFieldTemplateService.create(request.getCustomFields(), TestCaseTemplate.getId(),
                    TemplateConstants.FieldTemplateScene.TEST_CASE.name());
        }
    }

    /**
     * 获取该工作空间的系统模板
     * - 如果没有，则创建该工作空间模板，并关联默认的字段
     * - 如果有，则更新原来关联的 fieldId
     * @param customField
     */
    public void handleSystemFieldCreate(CustomField customField) {
        TestCaseTemplateWithBLOBs workspaceSystemTemplate = getWorkspaceSystemTemplate(customField.getProjectId());
        if (workspaceSystemTemplate == null) {
            createTemplateWithUpdateField(customField.getProjectId(), customField);
        } else {
            updateRelateWithUpdateField(workspaceSystemTemplate, customField);
        }
    }

    private void createTemplateWithUpdateField(String projectId, CustomField customField) {
        UpdateCaseFieldTemplateRequest request = new UpdateCaseFieldTemplateRequest();
        TestCaseTemplate testCaseTemplate = new TestCaseTemplate();
        testCaseTemplate.setName("default");
        testCaseTemplate.setType(TemplateConstants.TestCaseTemplateScene.functional.name());
        testCaseTemplate.setGlobal(false);
        testCaseTemplate.setSystem(true);
        testCaseTemplate.setProjectId(projectId);
        BeanUtils.copyBean(request, testCaseTemplate);
        List<CustomFieldTemplate> systemFieldCreateTemplate =
                customFieldTemplateService.getSystemFieldCreateTemplate(customField, TemplateConstants.FieldTemplateScene.TEST_CASE.name());
        request.setCustomFields(systemFieldCreateTemplate);
        add(request);
    }

    private void updateRelateWithUpdateField(TestCaseTemplateWithBLOBs template, CustomField customField) {
        CustomField globalField = customFieldService.getGlobalFieldByName(customField.getName());
        customFieldTemplateService.updateFieldIdByTemplate(template.getId(), globalField.getId(), customField.getId());
    }

    private TestCaseTemplateWithBLOBs getWorkspaceSystemTemplate(String workspaceId) {
        TestCaseTemplateExample example = new TestCaseTemplateExample();
        example.createCriteria()
                .andProjectIdEqualTo(workspaceId)
                .andSystemEqualTo(true);
        List<TestCaseTemplateWithBLOBs> testCaseTemplates = testCaseTemplateMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(testCaseTemplates)) {
            return testCaseTemplates.get(0);
        }
        return null;
    }

    private void checkExist(TestCaseTemplateWithBLOBs testCaseTemplate) {
        if (testCaseTemplate.getName() != null) {
            TestCaseTemplateExample example = new TestCaseTemplateExample();
            TestCaseTemplateExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(testCaseTemplate.getName())
                    .andProjectIdEqualTo(testCaseTemplate.getProjectId());
            if (StringUtils.isNotBlank(testCaseTemplate.getId())) {
                criteria.andIdNotEqualTo(testCaseTemplate.getId());
            }
            if (testCaseTemplateMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("template_already") + testCaseTemplate.getName());
            }
        }
    }

    public TestCaseTemplateWithBLOBs getDefaultTemplate(String projectId) {
        TestCaseTemplateExample example = new TestCaseTemplateExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andSystemEqualTo(true);
        List<TestCaseTemplateWithBLOBs> testCaseTemplates = testCaseTemplateMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(testCaseTemplates)) {
            return testCaseTemplates.get(0);
        } else {
            example.clear();
            example.createCriteria()
                    .andGlobalEqualTo(true);
            return testCaseTemplateMapper.selectByExampleWithBLOBs(example).get(0);
        }
    }

    public List<TestCaseTemplate> getOption(String projectId) {
        List<TestCaseTemplate> testCaseTemplates;
        TestCaseTemplateExample example = new TestCaseTemplateExample();
        if (StringUtils.isBlank(projectId)) {
            example.createCriteria().andGlobalEqualTo(true)
                    .andSystemEqualTo(true);
            return testCaseTemplateMapper.selectByExample(example);
        }
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andSystemNotEqualTo(true);
        testCaseTemplates = testCaseTemplateMapper.selectByExample(example);
        testCaseTemplates.add(getDefaultTemplate(projectId));
        return testCaseTemplates;
    }

    public Map<String,List<String>> getCaseLevelAndStatusMapByProjectId(String projectId){
        TestCaseTemplateDao template = getTemplate(projectId);
        List<CustomFieldDao> result = template.getCustomFields();

        Map<String, List<String>> returnMap = new HashMap<>();
        for (CustomFieldDao field : result) {
            if(StringUtils.equalsAnyIgnoreCase(field.getScene(),"TEST_CASE")){
                if(StringUtils.equalsAnyIgnoreCase(field.getName(),"用例等级")){
                    try {
                        JSONArray jsonArray = JSONArray.parseArray(field.getOptions());
                        List<String> values  = new ArrayList<>();
                        for (int i = 0;i < jsonArray.size();i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            values.add(obj.getString("value"));
                        }
                        returnMap.put("caseLevel",values);
                    }catch (Exception e){}
                }else if(StringUtils.equalsAnyIgnoreCase(field.getName(),"用例状态")){
                    try {
                        JSONArray jsonArray = JSONArray.parseArray(field.getOptions());
                        List<String> values  = new ArrayList<>();
                        for (int i = 0;i < jsonArray.size();i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            values.add(obj.getString("value"));
                        }
                        returnMap.put("caseStatus",values);
                    }catch (Exception e){}
                }
            }
        }
        return  returnMap;
    }

    public TestCaseTemplateDao getTemplate(String projectId) {
        Project project = projectService.getProjectById(projectId);
        String caseTemplateId = project.getCaseTemplateId();
        TestCaseTemplateWithBLOBs caseTemplate;
        TestCaseTemplateDao caseTemplateDao = new TestCaseTemplateDao();
        if (StringUtils.isNotBlank(caseTemplateId)) {
            caseTemplate = testCaseTemplateMapper.selectByPrimaryKey(caseTemplateId);
            if (caseTemplate == null) {
                caseTemplate = getDefaultTemplate(project.getWorkspaceId());
            }
        } else {
            caseTemplate = getDefaultTemplate(project.getWorkspaceId());
        }
        BeanUtils.copyBean(caseTemplateDao, caseTemplate);
        List<CustomFieldDao> result = customFieldService.getCustomFieldByTemplateId(caseTemplate.getId());
        caseTemplateDao.setCustomFields(result);
        return caseTemplateDao;
    }

    public String getLogDetails(String id) {
        TestCaseTemplateWithBLOBs templateWithBLOBs = testCaseTemplateMapper.selectByPrimaryKey(id);
        if (templateWithBLOBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(templateWithBLOBs, SystemReference.caseFieldColumns);
            columns.forEach(item ->{
                if(item.getColumnName().equals("steps") && item.getOriginalValue().toString().equals("[{\"num\":1,\"desc\":\"\",\"result\":\"\"}]")){
                    item.setOriginalValue("");
                }
            });
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(templateWithBLOBs.getId()), null, templateWithBLOBs.getName(), templateWithBLOBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
