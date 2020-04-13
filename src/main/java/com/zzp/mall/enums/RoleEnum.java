package com.zzp.mall.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    //管理员
    ADMIN(0),
    //普通用户
    CUSTOMER(1);

    Integer code;

    RoleEnum(Integer code) {
        this.code = code;
    }
}
