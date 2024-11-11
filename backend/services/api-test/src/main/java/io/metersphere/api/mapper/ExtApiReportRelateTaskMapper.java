package io.metersphere.api.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiReportRelateTaskMapper {

    List<String> selectDeleteTaskOrItem(@Param("ids") List<String> ids);
}
