package io.metersphere.controller.handler;


import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.UnauthorizedException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;


@RestControllerAdvice
public class RestControllerExceptionHandler {
    /*=========== Shiro 异常拦截==============*/
    @ExceptionHandler(ShiroException.class)
    public ResultHolder exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return ResultHolder.error(exception.getMessage());
    }

    /*=========== Shiro 异常拦截==============*/
    @ExceptionHandler(UnauthorizedException.class)
    public ResultHolder unauthorizedExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return ResultHolder.error(exception.getMessage());
    }


    @ExceptionHandler(SQLException.class)
    public ResultHolder sqlExceptionHandler(HttpServletRequest request, HttpServletResponse response, SQLException e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        LogUtil.error(e.getMessage(), e);
        return ResultHolder.error("Internal exception occured, please check logs or contact administrator.");
    }

    @ExceptionHandler(MSException.class)
    public ResultHolder msExceptionHandler(HttpServletRequest request, HttpServletResponse response, MSException e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResultHolder.error(e.getMessage());
    }
}
