package io.metersphere.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.service.MicroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/anonymous")
public class SystemDemoController {
    @Resource
    private MicroService microService;

    @GetMapping("/test-micro")
    public void test() {
        QueryMemberRequest param = new QueryMemberRequest();
        param.setProjectId("c872fa74-af59-4575-ac73-c978a9810f40");
        Pager<List<User>> forData = microService.postForData(MicroServiceName.PROJECT_MANAGEMENT, "/project/member/list/1/10", param, new TypeReference<>() {
        });

        List<ProjectDTO> result = microService.getForData(MicroServiceName.PROJECT_MANAGEMENT, "/project/get-owner-projects", new TypeReference<>() {
        });
        System.out.println(forData);
        System.out.println(result);
    }
}
