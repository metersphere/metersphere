package io.metersphere.base.mapper;

import io.metersphere.base.domain.UiTaskRefresh;
import org.apache.ibatis.annotations.Param;

public interface UiTaskRefreshMapper {

    UiTaskRefresh getByTaskKey(@Param("taskKey") String taskKey);

    void insert(UiTaskRefresh uiTaskRefresh);

    void update(@Param("refresh") UiTaskRefresh refresh);
}
