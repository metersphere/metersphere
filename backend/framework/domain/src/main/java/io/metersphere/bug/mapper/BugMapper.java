package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugMapper {
    long countByExample(BugExample example);

    int deleteByExample(BugExample example);

    int deleteByPrimaryKey(String id);

    int insert(Bug record);

    int insertSelective(Bug record);

    List<Bug> selectByExample(BugExample example);

    Bug selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Bug record, @Param("example") BugExample example);

    int updateByExample(@Param("record") Bug record, @Param("example") BugExample example);

    int updateByPrimaryKeySelective(Bug record);

    int updateByPrimaryKey(Bug record);

    int batchInsert(@Param("list") List<Bug> list);

    int batchInsertSelective(@Param("list") List<Bug> list, @Param("selective") Bug.Column ... selective);
}