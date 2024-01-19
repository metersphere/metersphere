package io.metersphere.project.service;

import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;

public interface CustomFunctionRunService {
    String run(CustomFunctionRunRequest runRequest);
}
