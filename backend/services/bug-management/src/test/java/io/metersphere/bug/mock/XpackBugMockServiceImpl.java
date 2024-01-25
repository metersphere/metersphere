package io.metersphere.bug.mock;

import io.metersphere.bug.dto.request.BugSyncRequest;
import io.metersphere.bug.service.XpackBugService;
import io.metersphere.project.domain.Project;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XpackBugMockServiceImpl implements XpackBugService {

    @Override
    public void syncPlatformBugsBySchedule() {

    }

    @Override
    public void syncPlatformBugs(Project project, BugSyncRequest request, String currentUser) {

    }

    @Override
    public List<OperationHistoryDTO> listHis(OperationHistoryRequest request) {
        return List.of();
    }
}
