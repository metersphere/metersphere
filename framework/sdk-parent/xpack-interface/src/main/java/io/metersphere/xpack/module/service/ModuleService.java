package io.metersphere.xpack.module.service;

public interface ModuleService {

    void updateModuleStatus(String key, String status);

    String getLogDetails(String key, String status);

}
