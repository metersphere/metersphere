package io.metersphere.system.dto.sdk;

import io.metersphere.sdk.util.CodingUtils;
import io.metersphere.system.dto.user.UserDTO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
public class SessionUser extends UserDTO implements Serializable {
    public static final String secret = "9a9rdqPlTqhpZzkq";
    public static final String iv = "1Av7hf9PgHusUHRm";

    private static final long serialVersionUID = -7149638440406959033L;
    private String csrfToken;
    private String sessionId;

    private SessionUser() {
    }

    public static SessionUser fromUser(UserDTO user, String sessionId) {
        SessionUser sessionUser = new SessionUser();
        BeanUtils.copyProperties(user, sessionUser);

        List<String> infos = Arrays.asList(user.getId(), RandomStringUtils.randomAlphabetic(6), sessionId, StringUtils.EMPTY + System.currentTimeMillis());
        sessionUser.csrfToken = CodingUtils.aesEncrypt(StringUtils.join(infos, "|"), secret, iv);
        sessionUser.sessionId = sessionId;
        return sessionUser;
    }

}
