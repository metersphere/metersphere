package io.metersphere.system.mapper;

import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExecTaskMapper {
    long countByExample(ExecTaskExample example);

    int deleteByExample(ExecTaskExample example);

    int deleteByPrimaryKey(String id);

    int insert(ExecTask record);

    int insertSelective(ExecTask record);

    List<ExecTask> selectByExample(ExecTaskExample example);

    ExecTask selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ExecTask record, @Param("example") ExecTaskExample example);

    int updateByExample(@Param("record") ExecTask record, @Param("example") ExecTaskExample example);

    int updateByPrimaryKeySelective(ExecTask record);

    int updateByPrimaryKey(ExecTask record);

    int batchInsert(@Param("list") List<ExecTask> list);

    int batchInsertSelective(@Param("list") List<ExecTask> list, @Param("selective") ExecTask.Column ... selective);
}