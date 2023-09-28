package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDebugModule;
import io.metersphere.api.domain.ApiDebugModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDebugModuleMapper {
    long countByExample(ApiDebugModuleExample example);

    int deleteByExample(ApiDebugModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDebugModule record);

    int insertSelective(ApiDebugModule record);

    List<ApiDebugModule> selectByExample(ApiDebugModuleExample example);

    ApiDebugModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDebugModule record, @Param("example") ApiDebugModuleExample example);

    int updateByExample(@Param("record") ApiDebugModule record, @Param("example") ApiDebugModuleExample example);

    int updateByPrimaryKeySelective(ApiDebugModule record);

    int updateByPrimaryKey(ApiDebugModule record);

    int batchInsert(@Param("list") List<ApiDebugModule> list);

    int batchInsertSelective(@Param("list") List<ApiDebugModule> list, @Param("selective") ApiDebugModule.Column ... selective);
}