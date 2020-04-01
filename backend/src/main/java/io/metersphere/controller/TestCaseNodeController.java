package io.metersphere.controller;

import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.dto.TestCaseNodeDTO;
import io.metersphere.service.TestCaseNodeService;
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

    @GetMapping("/list/plan/{planId}")
    public List<TestCaseNodeDTO> getNodeByPlanId(@PathVariable String planId){
        return testCaseNodeService.getNodeByPlanId(planId);
    }

    @PostMapping("/add")
    public int addNode(@RequestBody TestCaseNode node){
        return testCaseNodeService.addNode(node);
    }

    @PostMapping("/edit")
    public int editNode(@RequestBody TestCaseNode node){
        return testCaseNodeService.editNode(node);
    }

    @PostMapping("/delete")
    public int deleteNode(@RequestBody List<Integer> nodeIds){
        //nodeIds 包含删除节点ID及其所有子节点ID
        return testCaseNodeService.deleteNode(nodeIds);
    }
}
