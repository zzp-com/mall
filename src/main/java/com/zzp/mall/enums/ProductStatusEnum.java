package com.zzp.mall.enums;

import lombok.Getter;

@Getter
public enum ProductStatusEnum {
    ON_SALE(1,"商品在售"),
    OFF_SALE(2,"商品下架"),
    DELETE(3,"商品删除");

    Integer code;
    String desc;

    ProductStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
