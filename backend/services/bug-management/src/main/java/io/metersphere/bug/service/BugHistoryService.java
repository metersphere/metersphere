package io.metersphere.bug.service;

import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugHistoryService {

    public List<OperationHistoryDTO> list(OperationHistoryRequest request) {
        XpackBugService bugService = CommonBeanFactory.getBean(XpackBugService.class);
        if (bugService != null) {
            return bugService.listHis(request);
        }
        return List.of();
    }
}
