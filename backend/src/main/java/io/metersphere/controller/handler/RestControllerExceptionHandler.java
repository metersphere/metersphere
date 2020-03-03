package io.metersphere.controller.handler;


import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.ResultHolder;
import org.apache.shiro.ShiroException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestControllerAdvice
public class RestControllerExceptionHandler {
    /*=========== Shiro 异常拦截==============*/
    @ExceptionHandler(ShiroException.class)
    public ResultHolder exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return ResultHolder.error(exception.getMessage());
    }


    @ExceptionHandler(MSException.class)
    public ResultHolder msExceptionHandler(HttpServletRequest request, HttpServletResponse response, MSException e) {
        // 自定义异常返回200
        response.setStatus(HttpStatus.OK.value());
        return ResultHolder.error(e.getMessage());
    }
}
