package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.domain.ApiDefinitionBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionBlobMapper {
    long countByExample(ApiDefinitionBlobExample example);

    int deleteByExample(ApiDefinitionBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionBlob record);

    int insertSelective(ApiDefinitionBlob record);

    List<ApiDefinitionBlob> selectByExampleWithBLOBs(ApiDefinitionBlobExample example);

    List<ApiDefinitionBlob> selectByExample(ApiDefinitionBlobExample example);

    ApiDefinitionBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionBlob record, @Param("example") ApiDefinitionBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDefinitionBlob record, @Param("example") ApiDefinitionBlobExample example);

    int updateByExample(@Param("record") ApiDefinitionBlob record, @Param("example") ApiDefinitionBlobExample example);

    int updateByPrimaryKeySelective(ApiDefinitionBlob record);

    int updateByPrimaryKeyWithBLOBs(ApiDefinitionBlob record);

    int batchInsert(@Param("list") List<ApiDefinitionBlob> list);

    int batchInsertSelective(@Param("list") List<ApiDefinitionBlob> list, @Param("selective") ApiDefinitionBlob.Column ... selective);
}