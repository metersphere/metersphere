package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface ExtApiTestEnvironmentMapper {
    List<String> selectNameByIds(@Param("ids") Collection<String> envIdSet);

    String selectNameById(String id);
}
