package io.metersphere.functional.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalCaseRelationshipEdgeMapper {


    List<String> getGraphIds(@Param("ids") List<String> ids);
}
