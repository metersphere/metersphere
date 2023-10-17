package io.metersphere.system.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-12
 */
public interface ExtStatusItemMapper {
    List<String> getStatusItemIdByRefId(@Param("refId") String refId);
}
