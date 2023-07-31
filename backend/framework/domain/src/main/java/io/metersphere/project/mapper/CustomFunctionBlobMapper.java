package io.metersphere.project.mapper;

import io.metersphere.project.domain.CustomFunctionBlob;
import io.metersphere.project.domain.CustomFunctionBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFunctionBlobMapper {
    long countByExample(CustomFunctionBlobExample example);

    int deleteByExample(CustomFunctionBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(CustomFunctionBlob record);

    int insertSelective(CustomFunctionBlob record);

    List<CustomFunctionBlob> selectByExampleWithBLOBs(CustomFunctionBlobExample example);

    List<CustomFunctionBlob> selectByExample(CustomFunctionBlobExample example);

    CustomFunctionBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CustomFunctionBlob record, @Param("example") CustomFunctionBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomFunctionBlob record, @Param("example") CustomFunctionBlobExample example);

    int updateByExample(@Param("record") CustomFunctionBlob record, @Param("example") CustomFunctionBlobExample example);

    int updateByPrimaryKeySelective(CustomFunctionBlob record);

    int updateByPrimaryKeyWithBLOBs(CustomFunctionBlob record);

    int batchInsert(@Param("list") List<CustomFunctionBlob> list);

    int batchInsertSelective(@Param("list") List<CustomFunctionBlob> list, @Param("selective") CustomFunctionBlob.Column ... selective);
}