package io.metersphere.engine;

import java.util.Map;

public interface Engine {
    Long getStartTime();

    String getReportId();

    void start();

    void stop();

    Map<String, String> log();
}
