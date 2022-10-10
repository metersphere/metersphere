package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ModuleNode;
import org.apache.ibatis.annotations.Param;

public interface BaseModuleNodeMapper {

    void insert(@Param("tableName") String tableName, @Param("record") ModuleNode record);
}
