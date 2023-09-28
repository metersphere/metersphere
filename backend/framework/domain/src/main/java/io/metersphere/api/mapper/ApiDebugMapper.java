package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.domain.ApiDebugExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDebugMapper {
    long countByExample(ApiDebugExample example);

    int deleteByExample(ApiDebugExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDebug record);

    int insertSelective(ApiDebug record);

    List<ApiDebug> selectByExample(ApiDebugExample example);

    ApiDebug selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDebug record, @Param("example") ApiDebugExample example);

    int updateByExample(@Param("record") ApiDebug record, @Param("example") ApiDebugExample example);

    int updateByPrimaryKeySelective(ApiDebug record);

    int updateByPrimaryKey(ApiDebug record);

    int batchInsert(@Param("list") List<ApiDebug> list);

    int batchInsertSelective(@Param("list") List<ApiDebug> list, @Param("selective") ApiDebug.Column ... selective);
}