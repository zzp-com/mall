package com.zzp.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartProductVo {


    private Integer productId;
    /**
     * 购物车数量
     */
    private Integer quantity;

    private String productName;

    private String productSubtitle;

    private String productMainImage;

    private BigDecimal productPrice;

    private Integer productStatus;

    /**
     * 总价---数量*单价
     */
    private BigDecimal productTotalPrice;

    private Integer productStock;

    private Boolean productSelected;

}
