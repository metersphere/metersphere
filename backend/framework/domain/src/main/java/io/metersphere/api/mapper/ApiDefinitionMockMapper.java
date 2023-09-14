package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.domain.ApiDefinitionMockExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionMockMapper {
    long countByExample(ApiDefinitionMockExample example);

    int deleteByExample(ApiDefinitionMockExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDefinitionMock record);

    int insertSelective(ApiDefinitionMock record);

    List<ApiDefinitionMock> selectByExample(ApiDefinitionMockExample example);

    ApiDefinitionMock selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDefinitionMock record, @Param("example") ApiDefinitionMockExample example);

    int updateByExample(@Param("record") ApiDefinitionMock record, @Param("example") ApiDefinitionMockExample example);

    int updateByPrimaryKeySelective(ApiDefinitionMock record);

    int updateByPrimaryKey(ApiDefinitionMock record);

    int batchInsert(@Param("list") List<ApiDefinitionMock> list);

    int batchInsertSelective(@Param("list") List<ApiDefinitionMock> list, @Param("selective") ApiDefinitionMock.Column ... selective);
}