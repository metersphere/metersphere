package io.metersphere.controller;

import io.metersphere.base.domain.MinderExtraNode;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.service.MinderExtraNodeService;
import io.metersphere.track.request.MinderExtraNodeEditRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/minder/extra/node")
public class MinderExtraNodeController {

    @Resource
    MinderExtraNodeService minderExtraNodeService;

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void minderEdit(@RequestBody MinderExtraNodeEditRequest request) {
        minderExtraNodeService.batchEdit(request);
    }

    @GetMapping("/list/{groupId}/{parentId}")
    public List<MinderExtraNode> list(@PathVariable String groupId, @PathVariable String parentId) {
        return minderExtraNodeService.selectByParentId(parentId, groupId);
    }

}
