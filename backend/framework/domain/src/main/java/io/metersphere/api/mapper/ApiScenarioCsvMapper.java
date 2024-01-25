package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioCsv;
import io.metersphere.api.domain.ApiScenarioCsvExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioCsvMapper {
    long countByExample(ApiScenarioCsvExample example);

    int deleteByExample(ApiScenarioCsvExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioCsv record);

    int insertSelective(ApiScenarioCsv record);

    List<ApiScenarioCsv> selectByExample(ApiScenarioCsvExample example);

    ApiScenarioCsv selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioCsv record, @Param("example") ApiScenarioCsvExample example);

    int updateByExample(@Param("record") ApiScenarioCsv record, @Param("example") ApiScenarioCsvExample example);

    int updateByPrimaryKeySelective(ApiScenarioCsv record);

    int updateByPrimaryKey(ApiScenarioCsv record);

    int batchInsert(@Param("list") List<ApiScenarioCsv> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioCsv> list, @Param("selective") ApiScenarioCsv.Column ... selective);
}