package io.metersphere.base.mapper;

import io.metersphere.base.domain.FucTest;
import io.metersphere.base.domain.FucTestExample;
import io.metersphere.base.domain.FucTestWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FucTestMapper {
    long countByExample(FucTestExample example);

    int deleteByExample(FucTestExample example);

    int deleteByPrimaryKey(String id);

    int insert(FucTestWithBLOBs record);

    int insertSelective(FucTestWithBLOBs record);

    List<FucTestWithBLOBs> selectByExampleWithBLOBs(FucTestExample example);

    List<FucTest> selectByExample(FucTestExample example);

    FucTestWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FucTestWithBLOBs record, @Param("example") FucTestExample example);

    int updateByExampleWithBLOBs(@Param("record") FucTestWithBLOBs record, @Param("example") FucTestExample example);

    int updateByExample(@Param("record") FucTest record, @Param("example") FucTestExample example);

    int updateByPrimaryKeySelective(FucTestWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(FucTestWithBLOBs record);

    int updateByPrimaryKey(FucTest record);
}