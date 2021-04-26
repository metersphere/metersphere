package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.CustomField;
import io.metersphere.controller.request.QueryCustomFieldRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCustomFieldMapper {

    List<CustomField> list(@Param("request") QueryCustomFieldRequest request);

    List<CustomField> listRelate(@Param("request") QueryCustomFieldRequest request);

    List<String> listIds(@Param("request") QueryCustomFieldRequest request);
}
