package io.metersphere.engine;

import io.metersphere.base.domain.LoadTestWithBLOBs;

public interface Engine {
    boolean init(LoadTestWithBLOBs loadTest);

    void start();

    void stop();
}
