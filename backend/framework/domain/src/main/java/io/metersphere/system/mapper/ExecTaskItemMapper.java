package io.metersphere.system.mapper;

import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.domain.ExecTaskItemExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExecTaskItemMapper {
    long countByExample(ExecTaskItemExample example);

    int deleteByExample(ExecTaskItemExample example);

    int deleteByPrimaryKey(String id);

    int insert(ExecTaskItem record);

    int insertSelective(ExecTaskItem record);

    List<ExecTaskItem> selectByExample(ExecTaskItemExample example);

    ExecTaskItem selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ExecTaskItem record, @Param("example") ExecTaskItemExample example);

    int updateByExample(@Param("record") ExecTaskItem record, @Param("example") ExecTaskItemExample example);

    int updateByPrimaryKeySelective(ExecTaskItem record);

    int updateByPrimaryKey(ExecTaskItem record);

    int batchInsert(@Param("list") List<ExecTaskItem> list);

    int batchInsertSelective(@Param("list") List<ExecTaskItem> list, @Param("selective") ExecTaskItem.Column ... selective);
}