package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.IssueTemplate;
import io.metersphere.controller.request.BaseQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtIssueTemplateMapper {
    List<IssueTemplate> list(@Param("request") BaseQueryRequest request);
}
