package io.metersphere.project.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtFakeErrorMapper {

    List<String> selectByKeyword(@Param("keyword") String keyword);
}
