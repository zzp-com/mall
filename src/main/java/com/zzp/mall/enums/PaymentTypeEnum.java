package com.zzp.mall.enums;

import lombok.Getter;

@Getter
public enum PaymentTypeEnum {

    PAY_ONLONE(1);

    Integer code;

    PaymentTypeEnum(Integer code) {
        this.code = code;
    }
}
