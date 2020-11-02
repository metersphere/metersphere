package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.MessageTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtMessageMapper {
    List<MessageTask> searchMessageByTestId(@Param("testId") String testId);

    List<MessageTask> searchMessageByOrganizationId(@Param("organizationId") String organizationId);
}
