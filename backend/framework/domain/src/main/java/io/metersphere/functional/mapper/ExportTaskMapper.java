package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.ExportTask;
import io.metersphere.functional.domain.ExportTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExportTaskMapper {
    long countByExample(ExportTaskExample example);

    int deleteByExample(ExportTaskExample example);

    int deleteByPrimaryKey(String id);

    int insert(ExportTask record);

    int insertSelective(ExportTask record);

    List<ExportTask> selectByExample(ExportTaskExample example);

    ExportTask selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ExportTask record, @Param("example") ExportTaskExample example);

    int updateByExample(@Param("record") ExportTask record, @Param("example") ExportTaskExample example);

    int updateByPrimaryKeySelective(ExportTask record);

    int updateByPrimaryKey(ExportTask record);

    int batchInsert(@Param("list") List<ExportTask> list);

    int batchInsertSelective(@Param("list") List<ExportTask> list, @Param("selective") ExportTask.Column ... selective);
}