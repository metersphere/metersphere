package io.metersphere.base.mapper;

import io.metersphere.base.domain.OperatingLog;
import io.metersphere.base.domain.OperatingLogExample;
import io.metersphere.base.domain.OperatingLogWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OperatingLogMapper {
    long countByExample(OperatingLogExample example);

    int deleteByExample(OperatingLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(OperatingLogWithBLOBs record);

    int insertSelective(OperatingLogWithBLOBs record);

    List<OperatingLogWithBLOBs> selectByExampleWithBLOBs(OperatingLogExample example);

    List<OperatingLog> selectByExample(OperatingLogExample example);

    OperatingLogWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") OperatingLogWithBLOBs record, @Param("example") OperatingLogExample example);

    int updateByExampleWithBLOBs(@Param("record") OperatingLogWithBLOBs record, @Param("example") OperatingLogExample example);

    int updateByExample(@Param("record") OperatingLog record, @Param("example") OperatingLogExample example);

    int updateByPrimaryKeySelective(OperatingLogWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(OperatingLogWithBLOBs record);

    int updateByPrimaryKey(OperatingLog record);
}