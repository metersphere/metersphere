package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.MindAdditionalNode;
import io.metersphere.functional.domain.MindAdditionalNodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MindAdditionalNodeMapper {
    long countByExample(MindAdditionalNodeExample example);

    int deleteByExample(MindAdditionalNodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(MindAdditionalNode record);

    int insertSelective(MindAdditionalNode record);

    List<MindAdditionalNode> selectByExample(MindAdditionalNodeExample example);

    MindAdditionalNode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MindAdditionalNode record, @Param("example") MindAdditionalNodeExample example);

    int updateByExample(@Param("record") MindAdditionalNode record, @Param("example") MindAdditionalNodeExample example);

    int updateByPrimaryKeySelective(MindAdditionalNode record);

    int updateByPrimaryKey(MindAdditionalNode record);

    int batchInsert(@Param("list") List<MindAdditionalNode> list);

    int batchInsertSelective(@Param("list") List<MindAdditionalNode> list, @Param("selective") MindAdditionalNode.Column ... selective);
}