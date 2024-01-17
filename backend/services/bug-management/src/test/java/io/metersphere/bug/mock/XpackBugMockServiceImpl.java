package io.metersphere.bug.mock;

import io.metersphere.bug.dto.request.BugSyncRequest;
import io.metersphere.bug.service.XpackBugService;
import io.metersphere.project.domain.Project;
import org.springframework.stereotype.Service;

@Service
public class XpackBugMockServiceImpl implements XpackBugService {

    @Override
    public void syncPlatformBugsBySchedule() {

    }

    @Override
    public void syncPlatformBugs(Project project, BugSyncRequest request) {

    }
}
