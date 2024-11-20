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

import java.util.List;

@Component
public class NumGenerator {
    private static final long INIT = 100001L; // 代表从100001开始，各种domain的 num
    private static final long LIMIT = 1;

    private static Redisson redisson;
    private static StringRedisTemplate stringRedisTemplate;
    private static ProjectMapper projectMapper;

    private static final List<ApplicationNumScope> SUB_NUM = List.of(ApplicationNumScope.API_TEST_CASE, ApplicationNumScope.API_MOCK, ApplicationNumScope.TEST_PLAN_FUNCTION_CASE, ApplicationNumScope.TEST_PLAN_API_CASE, ApplicationNumScope.TEST_PLAN_API_SCENARIO);

    public static long nextNum(ApplicationNumScope scope) {
        return nextNum(scope.name(), scope);
    }

    /**
     * @param prefix 前缀: PROJECT_ID, 或者 PROJECT_ID + "_" + DOMAIN 例如接口用例的前缀为: 100001_12345
     * @param scope  用例类型
     */
    public static long nextNum(String prefix, ApplicationNumScope scope) {
        RIdGenerator idGenerator = redisson.getIdGenerator(prefix + "_" + scope.name());

        // 二级的用例
        if (SUB_NUM.contains(scope)) {
            // 每次都尝试初始化，容量为1，只有一个线程可以初始化成功
            if (!idGenerator.isExists()) {
                idGenerator.tryInit(1, LIMIT);
            }
            return Long.parseLong(prefix.split("_")[1] + StringUtils.leftPad(String.valueOf(idGenerator.nextId()), 3, "0"));
        } else {
            // 每次都尝试初始化，容量为1，只有一个线程可以初始化成功
            if (!idGenerator.isExists()) {
                idGenerator.tryInit(INIT, LIMIT);
            }
            return idGenerator.nextId();
        }
    }

    @QuartzScheduled(cron = "0 1 0 * * ?")
    public void cleanDeletedProjectResource() {
        for (ApplicationNumScope value : ApplicationNumScope.values()) {
            cleanDeletedProjectResource(value);
        }
    }

    private void cleanDeletedProjectResource(ApplicationNumScope scope) {
        final String SUFFIX = "}:allocation";
        final int SCAN_COUNT = 1000;

        ScanOptions options = ScanOptions.scanOptions()
                .match("*_" + scope.name())
                .count(SCAN_COUNT)
                .build();

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            cursor.forEachRemaining(key -> {
                // 如果 key 包含特定后缀，跳过处理
                if (StringUtils.contains(key, SUFFIX)) {
                    return;
                }

                // 提取 projectId 并跳过任务作用域的键
                String projectId = key.split("_")[0];
                if (StringUtils.equals(ApplicationNumScope.TASK.name(), projectId)) {
                    return;
                }

                // 检查项目是否存在，删除已删除项目的相关数据
                if (projectMapper.selectByPrimaryKey(projectId) == null) {
                    LogUtils.info("清理已经删除项目的 num 数据: {}", key);
                    stringRedisTemplate.delete(key);
                    stringRedisTemplate.delete("{" + key + SUFFIX);
                }
            });
        } catch (Exception e) {
            LogUtils.error("清理已删除项目资源时发生错误", e);
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
