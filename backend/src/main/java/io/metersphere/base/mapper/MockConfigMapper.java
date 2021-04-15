package io.metersphere.base.mapper;

import io.metersphere.base.domain.MockConfig;
import io.metersphere.base.domain.MockConfigExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface MockConfigMapper {
    long countByExample(MockConfigExample example);

    int deleteByExample(MockConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(MockConfig record);

    int insertSelective(MockConfig record);

    List<MockConfig> selectByExample(MockConfigExample example);

    MockConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MockConfig record, @Param("example") MockConfigExample example);

    int updateByExample(@Param("record") MockConfig record, @Param("example") MockConfigExample example);

    int updateByPrimaryKeySelective(MockConfig record);

    int updateByPrimaryKey(MockConfig record);
}