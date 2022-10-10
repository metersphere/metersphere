package io.metersphere.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.UserDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SystemDemoService {
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private MicroService microService;

//    @QuartzScheduled(cron = "0 0/1 * * * ?")
    public void test1() {
        UserDTO user = baseUserService.getUserDTO("admin");
        List<ProjectDTO> result = microService.runAsUser(user).getForData(MicroServiceName.PROJECT_MANAGEMENT, "/project/get-owner-projects", new TypeReference<>() {
        });
        System.out.println(result);
    }
}
