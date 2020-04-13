package com.zzp.mall.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zzp.mall.MallApplicationTests;
import com.zzp.mall.form.CartAddForm;
import com.zzp.mall.form.CartUpdateForm;
import com.zzp.mall.service.ICartService;
import com.zzp.mall.vo.CartVo;
import com.zzp.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class CartServiceImplTest extends MallApplicationTests {

    @Autowired
    private ICartService cartService;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void add() {
        CartAddForm cartAddForm = new CartAddForm(26, false);
        cartService.add(1, cartAddForm);
    }

    @Test
    public void list() {
        ResponseVo<CartVo> list = cartService.list(1);
        log.info("List = {}", gson.toJson(list));
    }

    @Test
    public void update() {
        CartUpdateForm form = new CartUpdateForm();
        form.setQuantity(10);
        form.setSelected(true);
        ResponseVo<CartVo> list = cartService.update(1, 26, form);
        log.info("List = {}", gson.toJson(list));
    }


    @Test
    public void delete() {
        ResponseVo<CartVo> list = cartService.delete(1, 26);
        log.info("List = {}", gson.toJson(list));
    }

}