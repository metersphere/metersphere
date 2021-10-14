package io.metersphere.base.mapper;

import io.metersphere.base.domain.MessageTask;
import io.metersphere.base.domain.MessageTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MessageTaskMapper {
    long countByExample(MessageTaskExample example);

    int deleteByExample(MessageTaskExample example);

    int deleteByPrimaryKey(String id);

    int insert(MessageTask record);

    int insertSelective(MessageTask record);

    List<MessageTask> selectByExampleWithBLOBs(MessageTaskExample example);

    List<MessageTask> selectByExample(MessageTaskExample example);

    MessageTask selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MessageTask record, @Param("example") MessageTaskExample example);

    int updateByExampleWithBLOBs(@Param("record") MessageTask record, @Param("example") MessageTaskExample example);

    int updateByExample(@Param("record") MessageTask record, @Param("example") MessageTaskExample example);

    int updateByPrimaryKeySelective(MessageTask record);

    int updateByPrimaryKeyWithBLOBs(MessageTask record);

    int updateByPrimaryKey(MessageTask record);
}