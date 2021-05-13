package io.metersphere.controller;

import io.metersphere.base.domain.Group;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.group.EditGroupRequest;
import io.metersphere.dto.GroupDTO;
import io.metersphere.service.GroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RequestMapping("/user/group")
@RestController
public class GroupController {

    @Resource
    private GroupService groupService;

    @PostMapping("/get/{goPage}/{pageSize}")
    public Pager<List<GroupDTO>> getGroupList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EditGroupRequest request) {
        request.setGoPage(goPage);
        request.setPageSize(pageSize);
        return groupService.getGroupList(request);
    }

    @PostMapping("/add")
    public Group addGroup(@RequestBody EditGroupRequest request) {
        return groupService.addGroup(request);
    }

    @PostMapping("/edit")
    public void editGroup(@RequestBody EditGroupRequest request) {
        groupService.editGroup(request);
    }

    @GetMapping("/delete/{id}")
    public void deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
    }

}
