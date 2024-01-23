package io.metersphere.bug.service;

import io.metersphere.bug.dto.request.BugHistoryPageRequest;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.dto.OperationHistoryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugHistoryService {

    public List<OperationHistoryDTO> list(BugHistoryPageRequest request) {
        XpackBugService bugService = CommonBeanFactory.getBean(XpackBugService.class);
        if (bugService != null) {
            return bugService.listHis(request);
        }
        return List.of();
    }
}
