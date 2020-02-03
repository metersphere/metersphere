package io.metersphere.user;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class SessionUtils {

    public static SessionUser getUser() {
        try {
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession();
            return (SessionUser) session.getAttribute("user");
        } catch (Exception e) {
            return null;
        }
    }
}
