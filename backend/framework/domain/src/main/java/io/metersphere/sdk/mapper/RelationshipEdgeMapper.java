package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.RelationshipEdge;
import io.metersphere.sdk.domain.RelationshipEdgeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RelationshipEdgeMapper {
    long countByExample(RelationshipEdgeExample example);

    int deleteByExample(RelationshipEdgeExample example);

    int deleteByPrimaryKey(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    int insert(RelationshipEdge record);

    int insertSelective(RelationshipEdge record);

    List<RelationshipEdge> selectByExample(RelationshipEdgeExample example);

    RelationshipEdge selectByPrimaryKey(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    int updateByExampleSelective(@Param("record") RelationshipEdge record, @Param("example") RelationshipEdgeExample example);

    int updateByExample(@Param("record") RelationshipEdge record, @Param("example") RelationshipEdgeExample example);

    int updateByPrimaryKeySelective(RelationshipEdge record);

    int updateByPrimaryKey(RelationshipEdge record);
}