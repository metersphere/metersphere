package io.metersphere.engine.docker;

import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineContext;
import org.springframework.stereotype.Service;

@Service
public class DockerTestEngine implements Engine {
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
