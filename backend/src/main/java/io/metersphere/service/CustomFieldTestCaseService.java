package io.metersphere.service;

import io.metersphere.base.domain.ext.CustomFieldResource;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldResourceDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldTestCaseService extends CustomFieldResourceService {

    private static final String TABLE_NAME = "custom_field_test_case";

    public void addFields(String resourceId, List<CustomFieldResource> addFields) {
        super.addFields(TABLE_NAME, resourceId, addFields);
    }

    public void editFields(String resourceId, List<CustomFieldResource> editFields) {
        super.editFields(TABLE_NAME, resourceId, editFields);
    }

    public int updateByPrimaryKeySelective(CustomFieldResource field) {
        return super.updateByPrimaryKeySelective(TABLE_NAME, field);
    }

    public int insert(CustomFieldResource field) {
        return super.insert(TABLE_NAME, field);
    }

    public void deleteByResourceId(String resourceId) {
        super.deleteByResourceId(TABLE_NAME, resourceId);
    }

    public void deleteByResourceIds(List<String> resourceIds) {
        super.deleteByResourceIds(TABLE_NAME, resourceIds);
    }

    public Map<String, List<CustomFieldDao>> getMapByResourceIds(List<String> resourceIds) {
        return super.getMapByResourceIds(TABLE_NAME, resourceIds);
    }

    public List<CustomFieldResource> getByResourceId(String resourceId) {
        return super.getByResourceId(TABLE_NAME, resourceId);
    }

    public void batchUpdateByResourceIds(List<String> resourceIds, CustomFieldResourceDTO customField) {
       super.batchUpdateByResourceIds(TABLE_NAME, resourceIds, customField);
    }

    public void batchInsertIfNotExists(List<String> ids, CustomFieldResourceDTO customField) {
        super.batchInsertIfNotExists(TABLE_NAME, ids, customField);
    }
}
