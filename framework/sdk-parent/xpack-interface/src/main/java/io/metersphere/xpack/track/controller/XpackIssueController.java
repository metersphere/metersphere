package io.metersphere.xpack.track.controller;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.xpack.track.service.XpackIssueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/xpack/issue")
public class XpackIssueController {

    @GetMapping("/sync/{projectId}")
    public boolean getPlatformIssue(@PathVariable String projectId) {
        XpackIssueService xpackIssueService = CommonBeanFactory.getBean(XpackIssueService.class);
        return xpackIssueService.syncThirdPartyIssues(projectId);
    }
}
