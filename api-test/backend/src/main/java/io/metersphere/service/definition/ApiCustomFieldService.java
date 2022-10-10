package io.metersphere.service.definition;


import io.metersphere.dto.CustomFieldDao;
import io.metersphere.service.CustomFieldResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiCustomFieldService extends CustomFieldResourceService {

    private static final String TABLE_NAME = "custom_field_api";

    public void deleteByResourceId(String resourceId) {
        super.deleteByResourceId(TABLE_NAME, resourceId);
    }

    public void deleteByResourceIds(List<String> resourceIds) {
        super.deleteByResourceIds(TABLE_NAME, resourceIds);
    }

    public Map<String, List<CustomFieldDao>> getMapByResourceIds(List<String> resourceIds) {
        return super.getMapByResourceIds(TABLE_NAME, resourceIds);
    }
}
