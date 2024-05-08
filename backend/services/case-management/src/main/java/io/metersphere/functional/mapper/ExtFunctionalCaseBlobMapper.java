package io.metersphere.functional.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtFunctionalCaseBlobMapper {

    void batchUpdateColumn(@Param("column") String column, @Param("ids") List<String> ids, @Param("value") byte[] value);
}
