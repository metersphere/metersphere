package io.metersphere.system.mapper;

import io.metersphere.system.domain.OperatingLog;
import io.metersphere.system.domain.OperatingLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OperatingLogMapper {
    long countByExample(OperatingLogExample example);

    int deleteByExample(OperatingLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(OperatingLog record);

    int insertSelective(OperatingLog record);

    List<OperatingLog> selectByExampleWithBLOBs(OperatingLogExample example);

    List<OperatingLog> selectByExample(OperatingLogExample example);

    OperatingLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") OperatingLog record, @Param("example") OperatingLogExample example);

    int updateByExampleWithBLOBs(@Param("record") OperatingLog record, @Param("example") OperatingLogExample example);

    int updateByExample(@Param("record") OperatingLog record, @Param("example") OperatingLogExample example);

    int updateByPrimaryKeySelective(OperatingLog record);

    int updateByPrimaryKeyWithBLOBs(OperatingLog record);

    int updateByPrimaryKey(OperatingLog record);
}