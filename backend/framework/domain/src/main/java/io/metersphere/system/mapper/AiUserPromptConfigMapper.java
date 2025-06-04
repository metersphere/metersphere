package io.metersphere.system.mapper;

import io.metersphere.system.domain.AiUserPromptConfig;
import io.metersphere.system.domain.AiUserPromptConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AiUserPromptConfigMapper {
    long countByExample(AiUserPromptConfigExample example);

    int deleteByExample(AiUserPromptConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(AiUserPromptConfig record);

    int insertSelective(AiUserPromptConfig record);

    List<AiUserPromptConfig> selectByExampleWithBLOBs(AiUserPromptConfigExample example);

    List<AiUserPromptConfig> selectByExample(AiUserPromptConfigExample example);

    AiUserPromptConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") AiUserPromptConfig record, @Param("example") AiUserPromptConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") AiUserPromptConfig record, @Param("example") AiUserPromptConfigExample example);

    int updateByExample(@Param("record") AiUserPromptConfig record, @Param("example") AiUserPromptConfigExample example);

    int updateByPrimaryKeySelective(AiUserPromptConfig record);

    int updateByPrimaryKeyWithBLOBs(AiUserPromptConfig record);

    int updateByPrimaryKey(AiUserPromptConfig record);

    int batchInsert(@Param("list") List<AiUserPromptConfig> list);

    int batchInsertSelective(@Param("list") List<AiUserPromptConfig> list, @Param("selective") AiUserPromptConfig.Column ... selective);
}