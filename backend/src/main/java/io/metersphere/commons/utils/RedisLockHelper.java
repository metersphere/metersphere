package io.metersphere.commons.utils;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class RedisLockHelper {

    public static final String LOCK_PREFIX = "REDIS_LOCK_";

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public boolean lock(String key) {
        return lock(key, 1000);
    }

    /**
     * @param key key
     * @param expire 过期时间，默认 1000 ms
     */
    public boolean lock(String key, int expire) {
        if (expire <= 0) {
            expire = 1000;
        }
        String lock = LOCK_PREFIX + key;
        //
        int LOCK_EXPIRE = expire;
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            long expireAt = System.currentTimeMillis() + LOCK_EXPIRE + 1;
            Boolean acquire = connection.setNX(lock.getBytes(), String.valueOf(expireAt).getBytes());

            if (BooleanUtils.toBoolean(acquire)) {
                return true;
            } else {
                byte[] value = connection.get(lock.getBytes());
                if (Objects.nonNull(value) && value.length > 0) {
                    long expireTime = Long.parseLong(new String(value));
                    if (expireTime < System.currentTimeMillis()) {
                        // in case the lock is expired
                        byte[] oldValue = connection.getSet(lock.getBytes(), String.valueOf(System.currentTimeMillis() + LOCK_EXPIRE + 1).getBytes());
                        // avoid dead lock
                        return Long.parseLong(new String(oldValue)) < System.currentTimeMillis();
                    }
                }
            }
            return false;
        });
        return BooleanUtils.toBoolean(result);
    }


    public boolean unlock(String key) {
        return redisTemplate.delete(key);
    }
}
