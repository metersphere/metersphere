package io.metersphere.bug.service;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugSyncExtraService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String SYNC_THIRD_PARTY_BUG_KEY = "MS:BUG:SYNC";
    private static final String SYNC_THIRD_PARTY_ISSUES_ERROR_KEY = "MS:BUG:SYNC:ERROR";

    /**
     * 设置手动同步缺陷唯一Key
     * @param projectId 项目ID
     */
    public void setSyncKey(String projectId) {
        stringRedisTemplate.opsForValue().set(SYNC_THIRD_PARTY_BUG_KEY + ":" + projectId, UUID.randomUUID().toString(), 60 * 10L, TimeUnit.SECONDS);
    }

    /**
     * 获取手动同步缺陷唯一Key
     * @param projectId 项目ID
     */
    public String getSyncKey(String projectId) {
        return stringRedisTemplate.opsForValue().get(SYNC_THIRD_PARTY_BUG_KEY + ":" + projectId);
    }

    /**
     * 删除手动同步缺陷唯一Key
     * @param projectId 项目ID
     */
    public void deleteSyncKey(String projectId) {
        stringRedisTemplate.delete(SYNC_THIRD_PARTY_BUG_KEY + ":" + projectId);
    }

    /**
     * 设置手动同步缺陷错误信息
     * @param projectId 项目ID
     */
    public void setSyncErrorMsg(String projectId, String errorMsg) {
        stringRedisTemplate.opsForValue().set(SYNC_THIRD_PARTY_ISSUES_ERROR_KEY + ":" + projectId, errorMsg, 60L, TimeUnit.SECONDS);
    }

    /**
     * 获取手动同步缺陷错误信息
     * @param projectId 项目ID
     */
    public String getSyncErrorMsg(String projectId) {
        return stringRedisTemplate.opsForValue().get(SYNC_THIRD_PARTY_ISSUES_ERROR_KEY + ":" + projectId);
    }

    /**
     * 删除手动同步缺陷错误信息
     * @param projectId 项目ID
     */
    public void deleteSyncErrorMsg(String projectId) {
        stringRedisTemplate.delete(SYNC_THIRD_PARTY_ISSUES_ERROR_KEY + ":" + projectId);
    }
}
