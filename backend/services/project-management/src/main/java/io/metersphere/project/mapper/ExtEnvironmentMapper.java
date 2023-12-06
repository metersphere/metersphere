package io.metersphere.project.mapper;

import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtEnvironmentMapper {

    List<Environment> selectByKeyword(@Param("keyword") String keyword, @Param("selectId") boolean selectId , @Param("projectId") String projectId);

    List<EnvironmentGroup> groupList(@Param("keyword") String keyword, @Param("projectId") String projectId);
}
