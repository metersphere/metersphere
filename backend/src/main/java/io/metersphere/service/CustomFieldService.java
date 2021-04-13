package io.metersphere.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldExample;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.ext.ExtCustomFieldMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.controller.request.QueryCustomFieldRequest;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldService {

    @Resource
    ExtCustomFieldMapper extCustomFieldMapper;

    @Resource
    CustomFieldMapper customFieldMapper;

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    public void add(CustomField customField) {
        checkExist(customField);
        customField.setId(UUID.randomUUID().toString());
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
        customFieldMapper.insert(customField);
    }

    public List<CustomField> list(QueryCustomFieldRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extCustomFieldMapper.list(request);
    }

    public Pager<List<CustomField>> listRelate(int goPage, int pageSize, QueryCustomFieldRequest request) {
        List<String> templateContainIds = request.getTemplateContainIds();
        if (CollectionUtils.isEmpty(templateContainIds)) {
            templateContainIds = new ArrayList<>();
        }
        templateContainIds.addAll(customFieldTemplateService.getCustomFieldIds(request.getTemplateId()));
        request.setTemplateContainIds(templateContainIds);
        Page<List<CustomField>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, extCustomFieldMapper.listRelate(request));
    }

    public void delete(String id) {
        customFieldMapper.deleteByPrimaryKey(id);
    }

    public void update(CustomField customField) {
        checkExist(customField);
        customField.setUpdateTime(System.currentTimeMillis());
        customFieldMapper.updateByPrimaryKeySelective(customField);
    }

    public List<String> listIds(QueryCustomFieldRequest request) {
        return extCustomFieldMapper.listIds(request);
    }

    private void checkExist(CustomField customField) {
        if (customField.getName() != null) {
            CustomFieldExample example = new CustomFieldExample();
            CustomFieldExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(customField.getName());
            if (StringUtils.isNotBlank(customField.getId())) {
                criteria.andIdNotEqualTo(customField.getId());
            }
            if (customFieldMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("custom_field_already") + customField.getName());
            }
        }
    }

}
