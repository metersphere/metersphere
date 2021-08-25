package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.MockExpectConfigWithBLOBs;

import java.util.List;

public interface ExtMockExpectConfigMapper {

    List<MockExpectConfigWithBLOBs> selectByProjectIdAndStatusIsOpen(String projectId);
}