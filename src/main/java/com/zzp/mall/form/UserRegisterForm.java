package com.zzp.mall.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class UserRegisterForm implements Serializable {

    //@NotBlank() 用于String 判断空格
    //@NotEmpty 用于集合
    //@NotNull
    @NotBlank()
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String email;
}
