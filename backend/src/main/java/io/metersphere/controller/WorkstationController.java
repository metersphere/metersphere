package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.base.domain.IssueTemplate;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.service.WorkspaceService;
import io.metersphere.service.WorkstationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RequestMapping("workstation")
@RestController
public class WorkstationController {

    @Resource
    private WorkstationService workstationService;

    @PostMapping("/creat_case_count/list")
    public Map<String,Integer> list() {

        return  workstationService.getMyCreatedCaseGroupContMap();
    }
}
