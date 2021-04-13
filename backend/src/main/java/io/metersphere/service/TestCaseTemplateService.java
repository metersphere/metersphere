package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseTemplateMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseTemplateMapper;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.UpdateCaseFieldTemplateRequest;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseTemplateService {

    @Resource
    ExtTestCaseTemplateMapper extTestCaseTemplateMapper;

    @Resource
    TestCaseTemplateMapper testCaseTemplateMapper;

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    public void add(UpdateCaseFieldTemplateRequest request) {
        checkExist(request);
        TestCaseTemplateWithBLOBs TestCaseTemplate = new TestCaseTemplateWithBLOBs();
        BeanUtils.copyBean(TestCaseTemplate, request);
        TestCaseTemplate.setId(UUID.randomUUID().toString());
        TestCaseTemplate.setCreateTime(System.currentTimeMillis());
        TestCaseTemplate.setUpdateTime(System.currentTimeMillis());
        testCaseTemplateMapper.insert(TestCaseTemplate);
        customFieldTemplateService.create(request.getCustomFields(), TestCaseTemplate.getId(),
                TemplateConstants.FieldTemplateScene.TEST_CASE.name());
    }

    public List<TestCaseTemplate> list(BaseQueryRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extTestCaseTemplateMapper.list(request);
    }

    public void delete(String id) {
        testCaseTemplateMapper.deleteByPrimaryKey(id);
    }

    public void update(UpdateCaseFieldTemplateRequest request) {
        checkExist(request);
        customFieldTemplateService.deleteByTemplateId(request.getId());
        TestCaseTemplateWithBLOBs TestCaseTemplate = new TestCaseTemplateWithBLOBs();
        BeanUtils.copyBean(TestCaseTemplate, request);
        TestCaseTemplate.setUpdateTime(System.currentTimeMillis());
        testCaseTemplateMapper.updateByPrimaryKeySelective(TestCaseTemplate);
        customFieldTemplateService.create(request.getCustomFields(), TestCaseTemplate.getId(),
                TemplateConstants.FieldTemplateScene.TEST_CASE.name());
    }

    private void checkExist(TestCaseTemplateWithBLOBs testCaseTemplate) {
        if (testCaseTemplate.getName() != null) {
            TestCaseTemplateExample example = new TestCaseTemplateExample();
            TestCaseTemplateExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(testCaseTemplate.getName());
            if (StringUtils.isNotBlank(testCaseTemplate.getId())) {
                criteria.andIdNotEqualTo(testCaseTemplate.getId());
            }
            if (testCaseTemplateMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("template_already") + testCaseTemplate.getName());
            }
        }
    }
}
