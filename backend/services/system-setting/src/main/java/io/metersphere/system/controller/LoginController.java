package io.metersphere.system.controller;


import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.UserSource;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.sdk.dto.LoginRequest;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.BaseUserService;
import io.metersphere.sdk.util.RsaKey;
import io.metersphere.sdk.util.RsaUtil;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.sdk.util.Translator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Tag(name="登陆")
public class LoginController {

    @Resource
    private BaseUserService baseUserService;


    @GetMapping(value = "/is-login")
    @Operation(summary = "是否登录")
    public ResultHolder isLogin(HttpServletResponse response) throws Exception {
        SessionUser user = SessionUtils.getUser();
        if (user != null) {
            UserDTO userDTO = baseUserService.getUserDTO(user.getId());
            if (StringUtils.isBlank(userDTO.getLanguage())) {
                userDTO.setLanguage(LocaleContextHolder.getLocale().toString());
            }

            baseUserService.autoSwitch(userDTO);
            SessionUser sessionUser = SessionUser.fromUser(userDTO, SessionUtils.getSessionId());
            SessionUtils.putUser(sessionUser);
            // 用户只有工作空间权限
            if (StringUtils.isBlank(sessionUser.getLastProjectId())) {
                sessionUser.setLastProjectId("no_such_project");
            }
            return ResultHolder.success(sessionUser);
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return ResultHolder.error(MsHttpResultCode.UNAUTHORIZED.getCode(), null);
    }

    @GetMapping(value = "/get-key")
    @Operation(summary = "获取公钥")
    public ResultHolder getKey() throws Exception {
        RsaKey rsaKey = RsaUtil.getRsaKey();
        return ResultHolder.success(rsaKey.getPublicKey());
    }

    @PostMapping(value = "/login")
    @Operation(summary = "登录")
    public ResultHolder login(@Validated @RequestBody LoginRequest request) {
        SessionUser sessionUser = SessionUtils.getUser();
        if (sessionUser != null) {
            if (!StringUtils.equals(sessionUser.getId(), request.getUsername())) {
                throw new MSException(Translator.get("please_logout_current_user"));
            }
        }
        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LOCAL.name());
        ResultHolder result = baseUserService.login(request);
        // 检查管理员是否需要改密码
        boolean changePassword = baseUserService.checkWhetherChangePasswordOrNot(request);
        result.setMessage(BooleanUtils.toStringTrueFalse(changePassword));
        return result;
    }

    @GetMapping(value = "/signout")
    @Operation(summary = "退出登录")
    public ResultHolder logout() throws Exception {
        if (SessionUtils.getUser() == null) {
            return ResultHolder.success("logout success");
        }
        baseUserService.saveLog(SessionUtils.getUserId(), HttpMethodConstants.GET.name(), "/signout", "登出成功", OperationLogType.LOGOUT.name());
        SecurityUtils.getSubject().logout();
        return ResultHolder.success("logout success");
    }

}
