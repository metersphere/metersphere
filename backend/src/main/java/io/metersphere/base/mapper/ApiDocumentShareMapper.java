package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDocumentShare;
import io.metersphere.base.domain.ApiDocumentShareExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDocumentShareMapper {
    long countByExample(ApiDocumentShareExample example);

    int deleteByExample(ApiDocumentShareExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDocumentShare record);

    int insertSelective(ApiDocumentShare record);

    List<ApiDocumentShare> selectByExampleWithBLOBs(ApiDocumentShareExample example);

    List<ApiDocumentShare> selectByExample(ApiDocumentShareExample example);

    ApiDocumentShare selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDocumentShare record, @Param("example") ApiDocumentShareExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDocumentShare record, @Param("example") ApiDocumentShareExample example);

    int updateByExample(@Param("record") ApiDocumentShare record, @Param("example") ApiDocumentShareExample example);

    int updateByPrimaryKeySelective(ApiDocumentShare record);

    int updateByPrimaryKeyWithBLOBs(ApiDocumentShare record);

    int updateByPrimaryKey(ApiDocumentShare record);
}