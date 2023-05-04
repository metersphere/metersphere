package io.metersphere.base.mapper.ext;

import java.util.List;

public interface ExtApiExecutionQueueMapper {
    List<String> selectIdByReportIdIsNull();
}

