package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCase;
import org.apache.ibatis.annotations.Param;

/**
 * @author wx
 */
public interface ExtFunctionalCaseMapper {
    FunctionalCase getMaxNumByProjectId(@Param("projectId") String projectId);

    Long getPos(@Param("projectId") String projectId);

   ;
}
