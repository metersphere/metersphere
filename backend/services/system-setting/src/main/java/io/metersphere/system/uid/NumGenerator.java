package io.metersphere.system.uid;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RIdGenerator;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class NumGenerator {
    private static final long INIT = 100001L; // 代表从100001开始，各种domain的 num
    private static final long SUB_INIT = 101L; // 代表从101开始，二级各种domain的 num
    private static final long LIMIT = 1;

    private static Redisson redisson;
    private static StringRedisTemplate stringRedisTemplate;
    private static ProjectMapper projectMapper;

    /**
     * @param prefix 前缀: PROJECT_ID, 或者 PROJECT_ID + "_" + DOMAIN 例如接口用例的前缀为: 100001_12345
     * @param scope  用例类型
     */
    public static long nextNum(String prefix, ApplicationNumScope scope) {
        RIdGenerator idGenerator = redisson.getIdGenerator(prefix + "_" + scope.name());
        // 每次都尝试初始化，容量为1，只有一个线程可以初始化成功
        if (scope.equals(ApplicationNumScope.API_TEST_CASE)) {
            // 二级的用例
            idGenerator.tryInit(Long.parseLong(prefix.split("_")[1] + SUB_INIT), LIMIT);
        } else {
            idGenerator.tryInit(INIT, LIMIT);
        }
        return idGenerator.nextId();
    }

    @QuartzScheduled(cron = "0 1 0 * * ?")
    public void cleanDeletedProjectResource() {
        for (ApplicationNumScope value : ApplicationNumScope.values()) {
            cleanDeletedProjectResource(value);
        }
    }

    private void cleanDeletedProjectResource(ApplicationNumScope value) {
        String suffix = "}:allocation";

        ScanOptions options = ScanOptions.scanOptions().match("*_" + value.name()).count(1000).build();
        try (
                Cursor<String> scan = stringRedisTemplate.scan(options)
        ) {
            while (scan.hasNext()) {
                String key = scan.next();
                if (StringUtils.contains(key, suffix)) {
                    continue;
                }
                Project project = projectMapper.selectByPrimaryKey(key.split("_")[0]);
                if (project == null) {
                    LogUtils.info("清理已经删除项目的num数据: " + key);
                    stringRedisTemplate.delete(key);
                    stringRedisTemplate.delete("{" + key + suffix);
                }
            }
        }
    }

    @Resource
    public void setRedisson(Redisson redisson) {
        NumGenerator.redisson = redisson;
    }

    @Resource
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        NumGenerator.stringRedisTemplate = stringRedisTemplate;
    }

    @Resource
    public void setProjectMapper(ProjectMapper projectMapper) {
        NumGenerator.projectMapper = projectMapper;
    }
}
