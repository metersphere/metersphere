package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseRelationshipEdge;
import io.metersphere.functional.domain.FunctionalCaseRelationshipEdgeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseRelationshipEdgeMapper {
    long countByExample(FunctionalCaseRelationshipEdgeExample example);

    int deleteByExample(FunctionalCaseRelationshipEdgeExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseRelationshipEdge record);

    int insertSelective(FunctionalCaseRelationshipEdge record);

    List<FunctionalCaseRelationshipEdge> selectByExample(FunctionalCaseRelationshipEdgeExample example);

    FunctionalCaseRelationshipEdge selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseRelationshipEdge record, @Param("example") FunctionalCaseRelationshipEdgeExample example);

    int updateByExample(@Param("record") FunctionalCaseRelationshipEdge record, @Param("example") FunctionalCaseRelationshipEdgeExample example);

    int updateByPrimaryKeySelective(FunctionalCaseRelationshipEdge record);

    int updateByPrimaryKey(FunctionalCaseRelationshipEdge record);

    int batchInsert(@Param("list") List<FunctionalCaseRelationshipEdge> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseRelationshipEdge> list, @Param("selective") FunctionalCaseRelationshipEdge.Column ... selective);
}