package io.metersphere.project.mapper;

import io.metersphere.project.domain.MessageTask;
import io.metersphere.project.domain.MessageTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MessageTaskMapper {
    long countByExample(MessageTaskExample example);

    int deleteByExample(MessageTaskExample example);

    int deleteByPrimaryKey(String id);

    int insert(MessageTask record);

    int insertSelective(MessageTask record);

    List<MessageTask> selectByExample(MessageTaskExample example);

    MessageTask selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MessageTask record, @Param("example") MessageTaskExample example);

    int updateByExample(@Param("record") MessageTask record, @Param("example") MessageTaskExample example);

    int updateByPrimaryKeySelective(MessageTask record);

    int updateByPrimaryKey(MessageTask record);

    int batchInsert(@Param("list") List<MessageTask> list);

    int batchInsertSelective(@Param("list") List<MessageTask> list, @Param("selective") MessageTask.Column ... selective);
}