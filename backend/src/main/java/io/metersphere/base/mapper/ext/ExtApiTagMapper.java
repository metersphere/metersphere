package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiTagRequest;
import io.metersphere.base.domain.ApiTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiTagMapper {
    List<ApiTag> list(@Param("request") ApiTagRequest request);
}