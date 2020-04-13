package com.zzp.mall.form;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 添加商品
 */
@Data
@AllArgsConstructor
public class CartAddForm {

    @NotNull
    private Integer productId;

    private Boolean selected;




}
