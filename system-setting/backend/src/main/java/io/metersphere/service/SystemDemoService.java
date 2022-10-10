package io.metersphere.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.utils.HttpHeaderUtils;
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
        HttpHeaderUtils.runAsUser(user);
        List<ProjectDTO> result = microService.getForData(MicroServiceName.PROJECT_MANAGEMENT, "/project/get-owner-projects", new TypeReference<>() {
        });
        HttpHeaderUtils.clearUser();
        System.out.println(result);
    }
}
