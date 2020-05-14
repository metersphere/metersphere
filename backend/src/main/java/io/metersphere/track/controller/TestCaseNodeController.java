package io.metersphere.track.controller;

import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.track.dto.TestCaseNodeDTO;
import io.metersphere.track.request.testcase.DragNodeRequest;
import io.metersphere.track.service.TestCaseNodeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RequestMapping("/case/node")
@RestController
public class TestCaseNodeController {

    @Resource
    TestCaseNodeService testCaseNodeService;

    @GetMapping("/list/{projectId}")
    public List<TestCaseNodeDTO> getNodeByProjectId(@PathVariable String projectId){
        return testCaseNodeService.getNodeTreeByProjectId(projectId);
    }

    @GetMapping("/list/all/plan/{planId}")
    public List<TestCaseNodeDTO> getAllNodeByPlanId(@PathVariable String planId){
        return testCaseNodeService.getAllNodeByPlanId(planId);
    }

    @GetMapping("/list/plan/{planId}")
    public List<TestCaseNodeDTO> getNodeByPlanId(@PathVariable String planId){
        return testCaseNodeService.getNodeByPlanId(planId);
    }

    @PostMapping("/add")
    public String addNode(@RequestBody TestCaseNode node){
        return testCaseNodeService.addNode(node);
    }

    @PostMapping("/edit")
    public int editNode(@RequestBody TestCaseNode node){
        return testCaseNodeService.editNode(node);
    }

    @PostMapping("/delete")
    public int deleteNode(@RequestBody List<String> nodeIds){
        //nodeIds 包含删除节点ID及其所有子节点ID
        return testCaseNodeService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    public void dragNode(@RequestBody DragNodeRequest node){
        testCaseNodeService.dragNode(node);
    }
}
