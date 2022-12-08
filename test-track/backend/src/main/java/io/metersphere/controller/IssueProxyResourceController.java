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

    @GetMapping(value = "/md/get/path")
    public ResponseEntity<byte[]> getFileByPath(@RequestParam ("path") String path,
                                                @RequestParam (value = "platform") String platform,
                                                @RequestParam (value = "workspaceId") String workspaceId) {
        return issueProxyResourceService.getMdImageByPath(path, platform, workspaceId);
    }
}
