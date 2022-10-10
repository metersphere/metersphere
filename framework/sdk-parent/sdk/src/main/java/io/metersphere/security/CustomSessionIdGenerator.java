package io.metersphere.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;

import java.io.Serializable;
import java.util.UUID;

public class CustomSessionIdGenerator implements SessionIdGenerator {
    @Override
    public Serializable generateId(Session session) {
        String threadSessionId = CustomSessionManager.threadSessionId.get();
        if (StringUtils.isNotBlank(threadSessionId)) {
            return threadSessionId;
        }
        return UUID.randomUUID().toString();
    }
}
