package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseRelationshipEdgeMapper {

    List<String> getGraphIdsByNodeIds(@Param("ids") List<String> ids);
}
