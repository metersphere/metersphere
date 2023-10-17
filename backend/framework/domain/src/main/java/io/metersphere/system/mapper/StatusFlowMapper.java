package io.metersphere.system.mapper;

import io.metersphere.system.domain.StatusFlow;
import io.metersphere.system.domain.StatusFlowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StatusFlowMapper {
    long countByExample(StatusFlowExample example);

    int deleteByExample(StatusFlowExample example);

    int deleteByPrimaryKey(String id);

    int insert(StatusFlow record);

    int insertSelective(StatusFlow record);

    List<StatusFlow> selectByExample(StatusFlowExample example);

    StatusFlow selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") StatusFlow record, @Param("example") StatusFlowExample example);

    int updateByExample(@Param("record") StatusFlow record, @Param("example") StatusFlowExample example);

    int updateByPrimaryKeySelective(StatusFlow record);

    int updateByPrimaryKey(StatusFlow record);

    int batchInsert(@Param("list") List<StatusFlow> list);

    int batchInsertSelective(@Param("list") List<StatusFlow> list, @Param("selective") StatusFlow.Column ... selective);
}