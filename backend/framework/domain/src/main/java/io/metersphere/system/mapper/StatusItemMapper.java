package io.metersphere.system.mapper;

import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.domain.StatusItemExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StatusItemMapper {
    long countByExample(StatusItemExample example);

    int deleteByExample(StatusItemExample example);

    int deleteByPrimaryKey(String id);

    int insert(StatusItem record);

    int insertSelective(StatusItem record);

    List<StatusItem> selectByExample(StatusItemExample example);

    StatusItem selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") StatusItem record, @Param("example") StatusItemExample example);

    int updateByExample(@Param("record") StatusItem record, @Param("example") StatusItemExample example);

    int updateByPrimaryKeySelective(StatusItem record);

    int updateByPrimaryKey(StatusItem record);

    int batchInsert(@Param("list") List<StatusItem> list);

    int batchInsertSelective(@Param("list") List<StatusItem> list, @Param("selective") StatusItem.Column ... selective);
}