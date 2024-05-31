package io.metersphere.system.mapper;

import io.metersphere.system.domain.PlatformSource;
import io.metersphere.system.domain.PlatformSourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PlatformSourceMapper {
    long countByExample(PlatformSourceExample example);

    int deleteByExample(PlatformSourceExample example);

    int deleteByPrimaryKey(String platform);

    int insert(PlatformSource record);

    int insertSelective(PlatformSource record);

    List<PlatformSource> selectByExampleWithBLOBs(PlatformSourceExample example);

    List<PlatformSource> selectByExample(PlatformSourceExample example);

    PlatformSource selectByPrimaryKey(String platform);

    int updateByExampleSelective(@Param("record") PlatformSource record, @Param("example") PlatformSourceExample example);

    int updateByExampleWithBLOBs(@Param("record") PlatformSource record, @Param("example") PlatformSourceExample example);

    int updateByExample(@Param("record") PlatformSource record, @Param("example") PlatformSourceExample example);

    int updateByPrimaryKeySelective(PlatformSource record);

    int updateByPrimaryKeyWithBLOBs(PlatformSource record);

    int updateByPrimaryKey(PlatformSource record);

    int batchInsert(@Param("list") List<PlatformSource> list);

    int batchInsertSelective(@Param("list") List<PlatformSource> list, @Param("selective") PlatformSource.Column ... selective);
}