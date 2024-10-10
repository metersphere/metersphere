package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDocShare;
import io.metersphere.api.domain.ApiDocShareExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiDocShareMapper {
    long countByExample(ApiDocShareExample example);

    int deleteByExample(ApiDocShareExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDocShare record);

    int insertSelective(ApiDocShare record);

    List<ApiDocShare> selectByExample(ApiDocShareExample example);

    ApiDocShare selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDocShare record, @Param("example") ApiDocShareExample example);

    int updateByExample(@Param("record") ApiDocShare record, @Param("example") ApiDocShareExample example);

    int updateByPrimaryKeySelective(ApiDocShare record);

    int updateByPrimaryKey(ApiDocShare record);

    int batchInsert(@Param("list") List<ApiDocShare> list);

    int batchInsertSelective(@Param("list") List<ApiDocShare> list, @Param("selective") ApiDocShare.Column ... selective);
}