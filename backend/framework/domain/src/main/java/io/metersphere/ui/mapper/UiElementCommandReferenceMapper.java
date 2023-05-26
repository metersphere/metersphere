package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiElementCommandReference;
import io.metersphere.ui.domain.UiElementCommandReferenceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiElementCommandReferenceMapper {
    long countByExample(UiElementCommandReferenceExample example);

    int deleteByExample(UiElementCommandReferenceExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiElementCommandReference record);

    int insertSelective(UiElementCommandReference record);

    List<UiElementCommandReference> selectByExample(UiElementCommandReferenceExample example);

    UiElementCommandReference selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiElementCommandReference record, @Param("example") UiElementCommandReferenceExample example);

    int updateByExample(@Param("record") UiElementCommandReference record, @Param("example") UiElementCommandReferenceExample example);

    int updateByPrimaryKeySelective(UiElementCommandReference record);

    int updateByPrimaryKey(UiElementCommandReference record);
}