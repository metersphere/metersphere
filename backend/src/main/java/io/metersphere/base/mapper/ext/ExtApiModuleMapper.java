package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiModule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiModuleMapper {
    int insertBatch(@Param("records") List<ApiModule> records);
}