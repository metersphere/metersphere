package io.metersphere.engine.kubernetes;

import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineContext;

public class KubernetesTestEngine implements Engine {
    private EngineContext context;

    @Override
    public boolean init(EngineContext context) {
        // todo 初始化操作
        this.context = context;
        return true;
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
