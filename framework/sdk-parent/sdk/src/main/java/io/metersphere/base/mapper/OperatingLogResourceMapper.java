package io.metersphere.base.mapper;

import io.metersphere.base.domain.OperatingLogResource;
import io.metersphere.base.domain.OperatingLogResourceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OperatingLogResourceMapper {
    long countByExample(OperatingLogResourceExample example);

    int deleteByExample(OperatingLogResourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(OperatingLogResource record);

    int insertSelective(OperatingLogResource record);

    List<OperatingLogResource> selectByExample(OperatingLogResourceExample example);

    OperatingLogResource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") OperatingLogResource record, @Param("example") OperatingLogResourceExample example);

    int updateByExample(@Param("record") OperatingLogResource record, @Param("example") OperatingLogResourceExample example);

    int updateByPrimaryKeySelective(OperatingLogResource record);

    int updateByPrimaryKey(OperatingLogResource record);
}