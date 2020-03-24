package io.metersphere.engine;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestWithBLOBs;

import java.util.List;

public interface Engine {
    boolean init(LoadTestWithBLOBs loadTest, FileMetadata fileMetadata, List<FileMetadata> csvFiles);

    void start();

    void stop();
}
