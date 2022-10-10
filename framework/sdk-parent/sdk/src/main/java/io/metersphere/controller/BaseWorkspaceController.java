package io.metersphere.controller;

import io.metersphere.base.domain.Workspace;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.service.BaseWorkspaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/workspace")
public class BaseWorkspaceController {
    @Resource
    private BaseWorkspaceService baseWorkspaceService;

    @GetMapping("/list/userworkspace")
    public List<Workspace> getWorkspaceListByUserId() {
        return baseWorkspaceService.getWorkspaceListByUserId(SessionUtils.getUserId());
    }
}
