package com.zzp.mall.exception;

import com.zzp.mall.enums.ResponseEnum;
import com.zzp.mall.vo.ResponseVo;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Objects;

import static com.zzp.mall.enums.ResponseEnum.ERROR;
import static com.zzp.mall.enums.ResponseEnum.NEED_LOGIN;

@ControllerAdvice
public class RuntimeExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)//状态码
    public ResponseVo handle(RuntimeException e){
        return ResponseVo.error(ERROR,e.getMessage());
    }

    /**
     * 用户登录
     * @return
     */
    @ExceptionHandler(UserLoginException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)//状态码
    public ResponseVo userLoginHandle(){
        return ResponseVo.error(NEED_LOGIN);
    }

    /**
     * 统一异常处理，表单验证处理
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)//状态码
    public ResponseVo notValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        return ResponseVo.error(ResponseEnum.PARAM_ERROR,
                Objects.requireNonNull(bindingResult.getFieldError()).getField()+bindingResult.getFieldError().getDefaultMessage());
    }



}
