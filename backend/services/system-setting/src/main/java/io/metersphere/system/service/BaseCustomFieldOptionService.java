package io.metersphere.system.service;

import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.domain.CustomFieldOptionExample;
import io.metersphere.system.dto.sdk.request.CustomFieldOptionRequest;
import io.metersphere.system.mapper.CustomFieldOptionMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseCustomFieldOptionService {
    @Resource
    private CustomFieldOptionMapper customFieldOptionMapper;

    public void deleteByFieldId(String fieldId) {
        CustomFieldOptionExample example = new CustomFieldOptionExample();
        example.createCriteria().andFieldIdEqualTo(fieldId);
        customFieldOptionMapper.deleteByExample(example);
    }

    public void deleteByFieldIds(List<String> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return;
        }
        CustomFieldOptionExample example = new CustomFieldOptionExample();
        example.createCriteria().andFieldIdIn(fieldIds);
        customFieldOptionMapper.deleteByExample(example);
    }

    public List<CustomFieldOption> getByFieldId(String fieldId) {
        CustomFieldOptionExample example = new CustomFieldOptionExample();
        example.createCriteria().andFieldIdEqualTo(fieldId);
        List<CustomFieldOption> options = customFieldOptionMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(options)) {
            options.sort(Comparator.comparing(CustomFieldOption::getPos));
        }
        return options;
    }

    public void addByFieldId(String fieldId, List<CustomFieldOption> customFieldOptions) {
        if (CollectionUtils.isEmpty(customFieldOptions)) {
            return;
        }
        customFieldOptions.stream().forEach(item -> {
            item.setFieldId(fieldId);
            item.setInternal(BooleanUtils.isTrue(item.getInternal()) ? true : false);
        });
        customFieldOptionMapper.batchInsert(customFieldOptions);
    }

    public void updateByFieldId(String fieldId, List<CustomFieldOptionRequest> customFieldOptionRequests) {
        List<CustomFieldOption> originOptions = getByFieldId(fieldId);
        // 查询原有选项
        Map<String, CustomFieldOption> optionMap =
                originOptions.stream().collect(Collectors.toMap(CustomFieldOption::getValue, i -> i));

        // 先删除选项，再添加
        deleteByFieldId(fieldId);

        List<CustomFieldOption> customFieldOptions = customFieldOptionRequests.stream().map(item -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            BeanUtils.copyBean(customFieldOption, item);
            if (optionMap.get(item.getValue()) != null) {
                // 保留选项是否是内置的选项
                customFieldOption.setInternal(optionMap.get(item.getValue()).getInternal());
            } else {
                customFieldOption.setInternal(false);
            }
            customFieldOption.setFieldId(fieldId);
            return customFieldOption;
        }).toList();
        if (CollectionUtils.isNotEmpty(customFieldOptions)) {
            customFieldOptionMapper.batchInsert(customFieldOptions);
        }
    }

    public List<CustomFieldOption> getByFieldIds(List<String> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return new ArrayList<>(0);
        }
        CustomFieldOptionExample example = new CustomFieldOptionExample();
        example.createCriteria().andFieldIdIn(fieldIds);
        List<CustomFieldOption> options = customFieldOptionMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(options)) {
            options.sort(Comparator.comparing(CustomFieldOption::getPos));
        }
        return options;
    }
}
