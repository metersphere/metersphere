package io.metersphere.project.mapper;

import io.metersphere.sdk.domain.Environment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtEnvironmentMapper {

    List<Environment> selectByKeyword(@Param("keyword") String keyword, @Param("selectId") boolean selectId , @Param("projectId") String projectId);
}
