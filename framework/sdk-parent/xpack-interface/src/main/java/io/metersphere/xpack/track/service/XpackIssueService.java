package io.metersphere.xpack.track.service;

import io.metersphere.xpack.track.dto.IssueSyncRequest;

public interface XpackIssueService {

    boolean syncThirdPartyIssues(IssueSyncRequest request);

    void syncThirdPartyIssues();
}
