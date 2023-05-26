package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiCustomCommandBlob;
import io.metersphere.ui.domain.UiCustomCommandBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiCustomCommandBlobMapper {
    long countByExample(UiCustomCommandBlobExample example);

    int deleteByExample(UiCustomCommandBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiCustomCommandBlob record);

    int insertSelective(UiCustomCommandBlob record);

    List<UiCustomCommandBlob> selectByExampleWithBLOBs(UiCustomCommandBlobExample example);

    List<UiCustomCommandBlob> selectByExample(UiCustomCommandBlobExample example);

    UiCustomCommandBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiCustomCommandBlob record, @Param("example") UiCustomCommandBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") UiCustomCommandBlob record, @Param("example") UiCustomCommandBlobExample example);

    int updateByExample(@Param("record") UiCustomCommandBlob record, @Param("example") UiCustomCommandBlobExample example);

    int updateByPrimaryKeySelective(UiCustomCommandBlob record);

    int updateByPrimaryKeyWithBLOBs(UiCustomCommandBlob record);
}