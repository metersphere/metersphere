package io.metersphere.sdk.controller.handler;

import io.metersphere.sdk.controller.handler.result.IResultCode;
import io.metersphere.sdk.controller.handler.result.MsHttpResultCode;
import io.metersphere.sdk.exception.MSException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                    .body(new ResultHolder(MsHttpResultCode.FAILED.getCode(),
                            MsHttpResultCode.FAILED.getMessage(), e.getMessage()));
        }

        if (errorCode instanceof MsHttpResultCode) {
            // 如果是 MsHttpResultCode，则设置响应的状态码，取状态码的后三位
            return ResponseEntity.status(errorCode.getCode() % 1000)
                    .body(new ResultHolder(errorCode.getCode(), errorCode.getMessage(), e.getMessage()));
        } else {
            // 响应码返回 500，设置业务状态码
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResultHolder(errorCode.getCode(), errorCode.getMessage(), e.getMessage()));
        }
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResultHolder> handlerMSException(Exception e) {
        return ResponseEntity.internalServerError()
                .body(new ResultHolder(MsHttpResultCode.FAILED.getCode(),
                        MsHttpResultCode.FAILED.getMessage(), e.getMessage()));
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


}
