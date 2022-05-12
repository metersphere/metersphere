package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.JarConfig;
import io.metersphere.controller.request.JarConfigRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtJarConfigMapper {
    List<JarConfig> list(@Param("request") JarConfigRequest request);

    int checkExist(@Param("request") JarConfigRequest request);
}
