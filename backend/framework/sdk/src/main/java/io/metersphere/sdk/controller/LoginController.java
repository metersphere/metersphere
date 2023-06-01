package io.metersphere.sdk.controller;


import io.metersphere.sdk.constants.UserSource;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.LoginRequest;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.BaseUserService;
import io.metersphere.sdk.util.RsaKey;
import io.metersphere.sdk.util.RsaUtil;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class LoginController {

    @Resource
    private BaseUserService baseUserService;


    @GetMapping(value = "/is-login")
    public ResultHolder isLogin() throws Exception {
        RsaKey rsaKey = RsaUtil.getRsaKey();
        SessionUser user = SessionUtils.getUser();
        if (user != null) {
            UserDTO userDTO = baseUserService.getUserDTO((String) MethodUtils.invokeMethod(user, "getId"));
            if (StringUtils.isBlank(userDTO.getLanguage())) {
                userDTO.setLanguage(LocaleContextHolder.getLocale().toString());
            }
            // todo 跳转用户
//            baseUserService.autoSwitch(userDTO);
            SessionUser sessionUser = SessionUser.fromUser(userDTO, SessionUtils.getSessionId());
            SessionUtils.putUser(sessionUser);
            // 用户只有工作空间权限
            if (StringUtils.isBlank(sessionUser.getLastProjectId())) {
                sessionUser.setLastProjectId("no_such_project");
            }
            return ResultHolder.success(sessionUser);
        }
        MSException.throwException(rsaKey.getPublicKey());
        //
        return null;
    }

    @PostMapping(value = "/signin")
    public ResultHolder login(@RequestBody LoginRequest request) {
        SessionUser sessionUser = SessionUtils.getUser();
        if (sessionUser != null) {
            if (!StringUtils.equals(sessionUser.getId(), request.getUsername())) {
                MSException.throwException(Translator.get("please_logout_current_user"));
            }
        }
        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LOCAL.name());
        ResultHolder result = baseUserService.login(request);
        // todo 登录是否提示修改密码
//        boolean changePassword = baseUserService.checkWhetherChangePasswordOrNot(request);
//        result.setMessage(BooleanUtils.toStringTrueFalse(changePassword));
        return result;
    }

    @GetMapping(value = "/signout")
    public void logout(HttpServletResponse response) throws Exception {
        SecurityUtils.getSubject().logout();
    }

}
