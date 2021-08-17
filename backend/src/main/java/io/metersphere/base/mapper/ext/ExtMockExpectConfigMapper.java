package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.MockExpectConfig;
import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtMockExpectConfigMapper {

    List<MockExpectConfigWithBLOBs> selectByProjectIdAndStatusIsOpen(String projectId);
}