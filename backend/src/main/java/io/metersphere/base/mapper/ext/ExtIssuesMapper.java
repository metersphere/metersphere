package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Issues;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtIssuesMapper {

    List<Issues> getIssues(@Param("caseId") String caseId, @Param("platform") String platform);
}
