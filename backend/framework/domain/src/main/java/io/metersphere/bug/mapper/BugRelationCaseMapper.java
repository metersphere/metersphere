package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.domain.BugRelationCaseExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugRelationCaseMapper {
    long countByExample(BugRelationCaseExample example);

    int deleteByExample(BugRelationCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugRelationCase record);

    int insertSelective(BugRelationCase record);

    List<BugRelationCase> selectByExample(BugRelationCaseExample example);

    BugRelationCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugRelationCase record, @Param("example") BugRelationCaseExample example);

    int updateByExample(@Param("record") BugRelationCase record, @Param("example") BugRelationCaseExample example);

    int updateByPrimaryKeySelective(BugRelationCase record);

    int updateByPrimaryKey(BugRelationCase record);

    int batchInsert(@Param("list") List<BugRelationCase> list);

    int batchInsertSelective(@Param("list") List<BugRelationCase> list, @Param("selective") BugRelationCase.Column ... selective);
}