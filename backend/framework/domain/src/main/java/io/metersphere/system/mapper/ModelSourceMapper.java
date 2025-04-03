package io.metersphere.system.mapper;

import io.metersphere.system.domain.ModelSource;
import io.metersphere.system.domain.ModelSourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ModelSourceMapper {
    long countByExample(ModelSourceExample example);

    int deleteByExample(ModelSourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(ModelSource record);

    int insertSelective(ModelSource record);

    List<ModelSource> selectByExample(ModelSourceExample example);

    ModelSource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ModelSource record, @Param("example") ModelSourceExample example);

    int updateByExample(@Param("record") ModelSource record, @Param("example") ModelSourceExample example);

    int updateByPrimaryKeySelective(ModelSource record);

    int updateByPrimaryKey(ModelSource record);

    int batchInsert(@Param("list") List<ModelSource> list);

    int batchInsertSelective(@Param("list") List<ModelSource> list, @Param("selective") ModelSource.Column ... selective);
}