package io.metersphere.project.mapper;

import io.metersphere.project.domain.MessageTaskBlob;
import io.metersphere.project.domain.MessageTaskBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MessageTaskBlobMapper {
    long countByExample(MessageTaskBlobExample example);

    int deleteByExample(MessageTaskBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(MessageTaskBlob record);

    int insertSelective(MessageTaskBlob record);

    List<MessageTaskBlob> selectByExampleWithBLOBs(MessageTaskBlobExample example);

    List<MessageTaskBlob> selectByExample(MessageTaskBlobExample example);

    MessageTaskBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MessageTaskBlob record, @Param("example") MessageTaskBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") MessageTaskBlob record, @Param("example") MessageTaskBlobExample example);

    int updateByExample(@Param("record") MessageTaskBlob record, @Param("example") MessageTaskBlobExample example);

    int updateByPrimaryKeySelective(MessageTaskBlob record);

    int updateByPrimaryKeyWithBLOBs(MessageTaskBlob record);

    int batchInsert(@Param("list") List<MessageTaskBlob> list);

    int batchInsertSelective(@Param("list") List<MessageTaskBlob> list, @Param("selective") MessageTaskBlob.Column ... selective);
}