package io.metersphere.xpack.track.controller;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.xpack.track.dto.IssueSyncRequest;
import io.metersphere.xpack.track.service.XpackIssueService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/xpack/issue")
public class XpackIssueController {

    @PostMapping("/sync")
    public boolean getPlatformIssue(@RequestBody IssueSyncRequest request) {
        XpackIssueService xpackIssueService = CommonBeanFactory.getBean(XpackIssueService.class);
        return xpackIssueService.syncThirdPartyIssues(request);
    }
}
