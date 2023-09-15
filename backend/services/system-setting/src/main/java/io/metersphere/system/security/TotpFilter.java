package io.metersphere.system.security;


import com.bastiaanjansen.otp.TOTPGenerator;
import io.metersphere.sdk.constants.MsHttpHeaders;
import io.metersphere.sdk.util.CommonBeanFactory;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;

public class TotpFilter extends AnonymousFilter {

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);

        // 请求头取出的token value
        String token = httpServletRequest.getHeader(MsHttpHeaders.OTP_TOKEN);
        // 校验 token
        validateToken(token);

        return true;
    }

    private void validateToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("token is empty");
        }

        TOTPGenerator totpGenerator = CommonBeanFactory.getBean(TOTPGenerator.class);
        if (!totpGenerator.verify(token)) {
            throw new RuntimeException("token is not valid");
        }
    }
}
