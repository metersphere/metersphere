package io.metersphere.engine;

public interface Engine {
    boolean init(EngineContext context);

    void start();

    void stop();
}
