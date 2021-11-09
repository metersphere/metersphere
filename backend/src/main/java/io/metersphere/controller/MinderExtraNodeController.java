package io.metersphere.controller;

import io.metersphere.base.domain.MinderExtraNode;
import io.metersphere.service.MinderExtraNodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/minder/extra/node")
public class MinderExtraNodeController {

    @Resource
    MinderExtraNodeService minderExtraNodeService;

//    @PostMapping("/batch/edit")
//    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
//    public void minderEdit(@RequestBody TestCaseMinderEditRequest.MinderExtraNodeEditRequest request) {
//        minderExtraNodeService.batchEdit(request);
//    }

    @GetMapping("/list/{groupId}/{parentId}")
    public List<MinderExtraNode> list(@PathVariable String groupId, @PathVariable String parentId) {
        return minderExtraNodeService.selectByParentId(parentId, groupId);
    }

}
