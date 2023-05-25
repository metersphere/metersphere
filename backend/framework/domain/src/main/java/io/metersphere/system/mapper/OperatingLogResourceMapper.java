package io.metersphere.system.mapper;

import io.metersphere.system.domain.OperatingLogResource;
import io.metersphere.system.domain.OperatingLogResourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

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