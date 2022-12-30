package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Quota;
import io.metersphere.quota.dto.CountDto;
import io.metersphere.quota.dto.QuotaResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtQuotaMapper {

    List<QuotaResult> listWorkspaceQuota(@Param("name") String name);

    Quota getWorkspaceQuota(@Param("workspaceId") String workspaceId);

    List<Quota> listUseDefaultOrgQuota();

    List<Quota> listUseDefaultWsQuota();

    List<Quota> listUseDefaultProjectQuota(String workspaceId);

    long countAPITest(@Param("workspaceIds") List<String> workspaceIds);

    long countLoadTest(@Param("projectIds") List<String> workspaceIds);

    long countAPIDefinition(@Param("projectIds") List<String> ids);

    long countAPIAutomation(@Param("projectIds") List<String> ids);

    List<QuotaResult> listProjectQuota(@Param("wsId") String workspaceId, @Param("name") String name);

    Quota getProjectQuota(String projectId);

    Long countMember(@Param("sourceId") String sourceId);

    Long countWorkspaceProject(@Param("workspaceId") String workspaceId);

    List<Quota> listQuotaBySourceIds(@Param("sourceIds") List<String> sourceIds);

    List<CountDto> listUserBySourceIds(@Param("sourceIds") List<String> sourceIds);

    long listUserByWorkspaceAndProjectIds(@Param("sourceIds") List<String> sourceIds, @Param("memberIds") List<String> memberIds);

    Quota getProjectQuotaSum(String workspaceId);
}
