package com.zzp.mall.service;

import com.zzp.mall.form.CartAddForm;
import com.zzp.mall.form.CartUpdateForm;
import com.zzp.mall.pojo.Cart;
import com.zzp.mall.vo.CartVo;
import com.zzp.mall.vo.ResponseVo;

import java.util.List;

public interface ICartService {

    ResponseVo<CartVo> add(Integer uid, CartAddForm form);

    ResponseVo<CartVo> list(Integer uid);

    ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm cartUpdateForm);

    ResponseVo<CartVo> delete(Integer uid, Integer productId);

    ResponseVo<CartVo> selectAll(Integer uid);

    ResponseVo<CartVo> unSelectAll(Integer uid);

    ResponseVo<Integer> sum(Integer uid);

    List<Cart> listForCar(Integer uid);
}
