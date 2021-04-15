package io.metersphere.base.mapper;

import io.metersphere.base.domain.MockExpectConfig;
import io.metersphere.base.domain.MockExpectConfigExample;
import io.metersphere.base.domain.MockExpectConfigWithBLOBs;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface MockExpectConfigMapper {
    long countByExample(MockExpectConfigExample example);

    int deleteByExample(MockExpectConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(MockExpectConfigWithBLOBs record);

    int insertSelective(MockExpectConfigWithBLOBs record);

    List<MockExpectConfigWithBLOBs> selectByExampleWithBLOBs(MockExpectConfigExample example);

    List<MockExpectConfig> selectByExample(MockExpectConfigExample example);

    MockExpectConfigWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MockExpectConfigWithBLOBs record, @Param("example") MockExpectConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") MockExpectConfigWithBLOBs record, @Param("example") MockExpectConfigExample example);

    int updateByExample(@Param("record") MockExpectConfig record, @Param("example") MockExpectConfigExample example);

    int updateByPrimaryKeySelective(MockExpectConfigWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(MockExpectConfigWithBLOBs record);

    int updateByPrimaryKey(MockExpectConfig record);
}