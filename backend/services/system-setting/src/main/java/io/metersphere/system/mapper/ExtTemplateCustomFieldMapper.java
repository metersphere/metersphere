package io.metersphere.system.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTemplateCustomFieldMapper {
    List<String> selectUsedFieldIds(@Param("fieldIds") List<String> list);
}
