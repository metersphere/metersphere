package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.CustomField;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseCustomFieldMapper {
    List<CustomField> getWorkspaceSystemFields(@Param("scene") String scene, @Param("workspaceId") String workspaceId);
}
