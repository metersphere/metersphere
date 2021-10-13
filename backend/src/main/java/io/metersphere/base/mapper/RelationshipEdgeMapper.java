package io.metersphere.base.mapper;

import io.metersphere.base.domain.RelationshipEdge;
import io.metersphere.base.domain.RelationshipEdgeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RelationshipEdgeMapper {
    long countByExample(RelationshipEdgeExample example);

    int deleteByExample(RelationshipEdgeExample example);

    int insert(RelationshipEdge record);

    int insertSelective(RelationshipEdge record);

    List<RelationshipEdge> selectByExample(RelationshipEdgeExample example);

    int updateByExampleSelective(@Param("record") RelationshipEdge record, @Param("example") RelationshipEdgeExample example);

    int updateByExample(@Param("record") RelationshipEdge record, @Param("example") RelationshipEdgeExample example);
}