package io.metersphere.xpack.track.service;

import io.metersphere.base.domain.Project;
import io.metersphere.xpack.track.dto.IssueSyncRequest;

public interface XpackIssueService {

    boolean syncThirdPartyIssues(Project project, IssueSyncRequest request);

    void syncThirdPartyIssues();
}
