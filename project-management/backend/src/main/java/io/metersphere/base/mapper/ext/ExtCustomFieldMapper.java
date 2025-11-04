package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.CustomField;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.request.QueryCustomFieldRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCustomFieldMapper {

    List<CustomField> list(@Param("request") QueryCustomFieldRequest request);

    List<CustomField> listRelate(@Param("request") QueryCustomFieldRequest request);

    List<String> listIds(@Param("request") QueryCustomFieldRequest request);

    void batchInsert(@Param("customFields") List<CustomField> customFields);

    /**
     * 更新旧数据的 fieldId 字段
     * @param customField 自定义字段
     */
    void updateIssueOldFieldId(@Param("customField") CustomFieldDao customField);
}
