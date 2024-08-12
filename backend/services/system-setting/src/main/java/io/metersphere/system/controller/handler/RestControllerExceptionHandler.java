package io.metersphere.system.controller.handler;

import io.metersphere.sdk.exception.IResultCode;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.lang.ShiroException;
import org.eclipse.jetty.io.EofException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class RestControllerExceptionHandler {

    /**
     * 处理数据校验异常
     * 返回具体字段的校验信息
     * http 状态码返回 400
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultHolder handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResultHolder.error(MsHttpResultCode.VALIDATE_FAILED.getCode(),
                MsHttpResultCode.VALIDATE_FAILED.getMessage(), errors);
    }

    /**
     * http 状态码返回405
     *
     * @param exception 异常信息
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultHolder handleHttpRequestMethodNotSupportedException(HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        return ResultHolder.error(HttpStatus.METHOD_NOT_ALLOWED.value(), exception.getMessage());
    }

    /**
     * 根据 MSException 中的 errorCode
     * 设置对应的 Http 状态码，以及业务状态码和错误提示
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MSException.class)
    public ResponseEntity<ResultHolder> handlerMSException(MSException e) {
        IResultCode errorCode = e.getErrorCode();
        if (errorCode == null) {
            // 如果抛出异常没有设置状态码，则返回错误 message
            return ResponseEntity.internalServerError()
                    .body(ResultHolder.error(MsHttpResultCode.FAILED.getCode(), e.getMessage()));
        }

        int code = errorCode.getCode();
        String message = errorCode.getMessage();
        message = Translator.get(message, message);

        if (errorCode instanceof MsHttpResultCode) {
            // 如果是 MsHttpResultCode，则设置响应的状态码，取状态码的后三位
            if (errorCode.equals(MsHttpResultCode.NOT_FOUND)) {
                message = getNotFoundMessage(message);
            }
            return ResponseEntity.status(code % 1000)
                    .body(ResultHolder.error(code, message, e.getMessage()));
        } else {
            // 响应码返回 500，设置业务状态码
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultHolder.error(code, Translator.get(message, message), e.getMessage()));
        }
    }

    /**
     * 当抛出 NOT_FOUND，拼接资源名称
     *
     * @param message
     * @return
     */
    private static String getNotFoundMessage(String message) {
        String resourceName = ServiceUtils.getResourceName();
        if (StringUtils.isNotBlank(resourceName)) {
            message = String.format(message, Translator.get(resourceName, resourceName));
        } else {
            message = String.format(message, Translator.get("resource.name"));
        }
        ServiceUtils.clearResourceName();
        return message;
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResultHolder> handleException(Exception e) {
        return ResponseEntity.internalServerError()
                .body(ResultHolder.error(MsHttpResultCode.FAILED.getCode(),
                        e.getMessage(), getStackTraceAsString(e)));
    }

    @ExceptionHandler({EofException.class})
    public ResponseEntity<Object> handleEofException(HttpServletRequest request, Exception e) {
        String requestURI = request.getRequestURI();
        if (StringUtils.startsWith(requestURI, "/assets")
                || StringUtils.startsWith(requestURI, "/fonts")
                || StringUtils.startsWith(requestURI, "/images")
                || StringUtils.startsWith(requestURI, "/templates")) {
            return ResponseEntity.internalServerError().body(null);
        }
        return ResponseEntity.internalServerError()
                .body(ResultHolder.error(MsHttpResultCode.FAILED.getCode(),
                        e.getMessage(), getStackTraceAsString(e)));
    }

    /*=========== Shiro 异常拦截==============*/
    @ExceptionHandler(ShiroException.class)
    public ResultHolder exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return ResultHolder.error(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
    }

    /*=========== Shiro 异常拦截==============*/
    @ExceptionHandler(UnauthorizedException.class)
    public ResultHolder unauthorizedExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return ResultHolder.error(HttpStatus.FORBIDDEN.value(), exception.getMessage());
    }

    /**
     * 格式化异常信息
     * 当出现未知异常时，将错误栈信息格式化返回
     *
     * @param e
     * @return
     */
    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
