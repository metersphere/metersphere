package io.metersphere.runner;

public interface Engine {
    boolean init(EngineContext context);

    void start();

    void stop();
}
