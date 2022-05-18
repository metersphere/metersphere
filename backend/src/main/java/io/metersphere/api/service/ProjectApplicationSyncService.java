package io.metersphere.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.metersphere.base.domain.ProjectApplication;

public interface ProjectApplicationSyncService {
    // 初始化引用关系
    ProjectApplication initProjectApplicationAboutWorkstation(ProjectApplication projectApplication ) throws JsonProcessingException;
}
