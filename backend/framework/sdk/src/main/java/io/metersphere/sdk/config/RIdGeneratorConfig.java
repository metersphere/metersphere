package io.metersphere.sdk.config;

import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.ResourceType;
import io.metersphere.sdk.service.BaseProjectService;
import jakarta.annotation.Resource;
import org.redisson.Redisson;
import org.redisson.api.RIdGenerator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RIdGeneratorConfig implements ApplicationRunner {
    @Resource
    private Redisson redisson;

    @Resource
    private BaseProjectService baseProjectService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Project> projects = baseProjectService.selectProjectList();
        projects.forEach(project -> {
            for (ResourceType resourceType : ResourceType.values()) {
                // todo 项目也需要一个num
                RIdGenerator idGenerator = redisson.getIdGenerator(resourceType.name() + ":" + project.getId());
                long capacity = 5000; // 一次性分配容量，默认是5000
                // todo 根据数据库现存的最大num，初始化
                long init = 1000001L; // 代表从100_0000_100_0001开始，项目的num
                idGenerator.tryInit(init, capacity);
                long nextId = idGenerator.nextId();
                System.out.println(nextId);
            }
        });
    }
}
