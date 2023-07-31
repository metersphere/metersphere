package io.metersphere.project.mapper;

import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.CustomFunctionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFunctionMapper {
    long countByExample(CustomFunctionExample example);

    int deleteByExample(CustomFunctionExample example);

    int deleteByPrimaryKey(String id);

    int insert(CustomFunction record);

    int insertSelective(CustomFunction record);

    List<CustomFunction> selectByExample(CustomFunctionExample example);

    CustomFunction selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CustomFunction record, @Param("example") CustomFunctionExample example);

    int updateByExample(@Param("record") CustomFunction record, @Param("example") CustomFunctionExample example);

    int updateByPrimaryKeySelective(CustomFunction record);

    int updateByPrimaryKey(CustomFunction record);

    int batchInsert(@Param("list") List<CustomFunction> list);

    int batchInsertSelective(@Param("list") List<CustomFunction> list, @Param("selective") CustomFunction.Column ... selective);
}