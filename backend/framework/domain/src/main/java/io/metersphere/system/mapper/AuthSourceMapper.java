package io.metersphere.system.mapper;

import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.domain.AuthSourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuthSourceMapper {
    long countByExample(AuthSourceExample example);

    int deleteByExample(AuthSourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(AuthSource record);

    int insertSelective(AuthSource record);

    List<AuthSource> selectByExampleWithBLOBs(AuthSourceExample example);

    List<AuthSource> selectByExample(AuthSourceExample example);

    AuthSource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") AuthSource record, @Param("example") AuthSourceExample example);

    int updateByExampleWithBLOBs(@Param("record") AuthSource record, @Param("example") AuthSourceExample example);

    int updateByExample(@Param("record") AuthSource record, @Param("example") AuthSourceExample example);

    int updateByPrimaryKeySelective(AuthSource record);

    int updateByPrimaryKeyWithBLOBs(AuthSource record);

    int updateByPrimaryKey(AuthSource record);

    int batchInsert(@Param("list") List<AuthSource> list);

    int batchInsertSelective(@Param("list") List<AuthSource> list, @Param("selective") AuthSource.Column ... selective);
}