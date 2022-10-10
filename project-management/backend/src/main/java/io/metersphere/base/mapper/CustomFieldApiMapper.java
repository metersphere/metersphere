package io.metersphere.base.mapper;

import io.metersphere.base.domain.CustomFieldApi;
import io.metersphere.base.domain.CustomFieldApiExample;
import io.metersphere.base.domain.CustomFieldApiKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomFieldApiMapper {
    long countByExample(CustomFieldApiExample example);

    int deleteByExample(CustomFieldApiExample example);

    int deleteByPrimaryKey(CustomFieldApiKey key);

    int insert(CustomFieldApi record);

    int insertSelective(CustomFieldApi record);

    List<CustomFieldApi> selectByExampleWithBLOBs(CustomFieldApiExample example);

    List<CustomFieldApi> selectByExample(CustomFieldApiExample example);

    CustomFieldApi selectByPrimaryKey(CustomFieldApiKey key);

    int updateByExampleSelective(@Param("record") CustomFieldApi record, @Param("example") CustomFieldApiExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomFieldApi record, @Param("example") CustomFieldApiExample example);

    int updateByExample(@Param("record") CustomFieldApi record, @Param("example") CustomFieldApiExample example);

    int updateByPrimaryKeySelective(CustomFieldApi record);

    int updateByPrimaryKeyWithBLOBs(CustomFieldApi record);

    int updateByPrimaryKey(CustomFieldApi record);
}