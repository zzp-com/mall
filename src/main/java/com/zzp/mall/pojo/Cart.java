package com.zzp.mall.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cart {

    private Integer productId;
    /**
     * 购物车数量
     */
    private Integer quantity;

    private Boolean productSelected;
}
