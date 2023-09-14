package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioModule;
import io.metersphere.api.domain.ApiScenarioModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioModuleMapper {
    long countByExample(ApiScenarioModuleExample example);

    int deleteByExample(ApiScenarioModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioModule record);

    int insertSelective(ApiScenarioModule record);

    List<ApiScenarioModule> selectByExample(ApiScenarioModuleExample example);

    ApiScenarioModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioModule record, @Param("example") ApiScenarioModuleExample example);

    int updateByExample(@Param("record") ApiScenarioModule record, @Param("example") ApiScenarioModuleExample example);

    int updateByPrimaryKeySelective(ApiScenarioModule record);

    int updateByPrimaryKey(ApiScenarioModule record);

    int batchInsert(@Param("list") List<ApiScenarioModule> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioModule> list, @Param("selective") ApiScenarioModule.Column ... selective);
}