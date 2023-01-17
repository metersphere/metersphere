package io.metersphere.job.schedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class CleanSessionJob {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisIndexedSessionRepository redisIndexedSessionRepository;


    /**
     * 清理没有绑定user的session
     * redisson 有时会把ttl设置成-1 https://github.com/redisson/redisson/issues/4200
     */
    @QuartzScheduled(cron = "0 2 0 * * ?")
    public void cleanSession() {
        Map<String, Long> userCount = new HashMap<>();
        ScanOptions options = ScanOptions.scanOptions().match("spring:session:sessions:*").count(1000).build();
        try (
                Cursor<String> scan = stringRedisTemplate.scan(options)
        ) {
            while (scan.hasNext()) {
                String key = scan.next();
                if (StringUtils.contains(key, "spring:session:sessions:expires:")) {
                    continue;
                }
                Boolean exists = stringRedisTemplate.opsForHash().hasKey(key, "sessionAttr:user");
                if (!exists) {
                    stringRedisTemplate.delete(key);
                } else {
                    Object user = redisIndexedSessionRepository.getSessionRedisOperations().opsForHash().get(key, "sessionAttr:user");
                    Long expire = redisIndexedSessionRepository.getSessionRedisOperations().getExpire(key);
                    String userId = (String) MethodUtils.invokeMethod(user, "getId");
                    Long count = userCount.getOrDefault(userId, 0L);
                    count++;
                    userCount.put(userId, count);
                    LogUtil.info(key + " : " + userId + " 过期时间: " + expire);
                    if (expire != null && expire.intValue() == -1) {
                        redisIndexedSessionRepository.getSessionRedisOperations().expire(key, Duration.of(30, ChronoUnit.SECONDS));
                    }
                }
            }

            LogUtil.info(JSON.toJSONString(userCount));
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
