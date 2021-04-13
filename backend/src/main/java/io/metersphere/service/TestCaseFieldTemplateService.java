package io.metersphere.service;

import io.metersphere.base.domain.TestCaseFieldTemplate;
import io.metersphere.base.domain.TestCaseFieldTemplateWithBLOBs;
import io.metersphere.base.mapper.TestCaseFieldTemplateMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseFieldTemplateMapper;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.UpdateCaseFieldTemplateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseFieldTemplateService {

    @Resource
    ExtTestCaseFieldTemplateMapper extTestCaseFieldTemplateMapper;

    @Resource
    TestCaseFieldTemplateMapper testCaseFieldTemplateMapper;

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    public void add(UpdateCaseFieldTemplateRequest request) {
        TestCaseFieldTemplateWithBLOBs testCaseFieldTemplate = new TestCaseFieldTemplateWithBLOBs();
        BeanUtils.copyBean(testCaseFieldTemplate, request);
        testCaseFieldTemplate.setId(UUID.randomUUID().toString());
        testCaseFieldTemplate.setCreateTime(System.currentTimeMillis());
        testCaseFieldTemplate.setUpdateTime(System.currentTimeMillis());
        testCaseFieldTemplateMapper.insert(testCaseFieldTemplate);
        customFieldTemplateService.create(request.getCustomFields(), testCaseFieldTemplate.getId(),
                TemplateConstants.FieldTemplateScene.TEST_CASE.name());
    }

    public List<TestCaseFieldTemplate> list(BaseQueryRequest request) {
        return extTestCaseFieldTemplateMapper.list(request);
    }

    public void delete(String id) {
        testCaseFieldTemplateMapper.deleteByPrimaryKey(id);
    }

    public void update(UpdateCaseFieldTemplateRequest request) {
        customFieldTemplateService.deleteByTemplateId(request.getId());
        TestCaseFieldTemplateWithBLOBs testCaseFieldTemplate = new TestCaseFieldTemplateWithBLOBs();
        BeanUtils.copyBean(testCaseFieldTemplate, request);
        testCaseFieldTemplate.setUpdateTime(System.currentTimeMillis());
        testCaseFieldTemplateMapper.updateByPrimaryKeySelective(testCaseFieldTemplate);
        customFieldTemplateService.create(request.getCustomFields(), testCaseFieldTemplate.getId(),
                TemplateConstants.FieldTemplateScene.TEST_CASE.name());
    }
}
