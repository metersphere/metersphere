package io.metersphere.system.service;


import java.util.Map;

public interface BaseCleanUpReport {
    void cleanReport(Map<String, String> map, String projectId);
}
