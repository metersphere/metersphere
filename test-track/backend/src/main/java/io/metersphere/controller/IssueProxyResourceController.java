package io.metersphere.controller;

import io.metersphere.service.wapper.IssueProxyResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/resource")
public class IssueProxyResourceController {
    @Resource
    IssueProxyResourceService issueProxyResourceService;

    @GetMapping(value = "/md/get/url")
    public ResponseEntity<byte[]> getFileByUrl(@RequestParam ("url") String url, @RequestParam (value = "platform", required = false) String platform,
                                               @RequestParam (value = "workspace_id", required = false) String workspaceId) {
        return issueProxyResourceService.getMdImageByUrl(url, platform, workspaceId);
    }
}
