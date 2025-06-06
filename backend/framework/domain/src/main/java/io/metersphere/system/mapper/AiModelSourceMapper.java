package io.metersphere.system.mapper;

import io.metersphere.system.domain.AiModelSource;
import io.metersphere.system.domain.AiModelSourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AiModelSourceMapper {
    long countByExample(AiModelSourceExample example);

    int deleteByExample(AiModelSourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(AiModelSource record);

    int insertSelective(AiModelSource record);

    List<AiModelSource> selectByExample(AiModelSourceExample example);

    AiModelSource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") AiModelSource record, @Param("example") AiModelSourceExample example);

    int updateByExample(@Param("record") AiModelSource record, @Param("example") AiModelSourceExample example);

    int updateByPrimaryKeySelective(AiModelSource record);

    int updateByPrimaryKey(AiModelSource record);

    int batchInsert(@Param("list") List<AiModelSource> list);

    int batchInsertSelective(@Param("list") List<AiModelSource> list, @Param("selective") AiModelSource.Column ... selective);
}