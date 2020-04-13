package com.zzp.mall.service;

import com.github.pagehelper.PageInfo;
import com.zzp.mall.form.ShippingForm;
import com.zzp.mall.vo.ResponseVo;

import java.util.Map;

public interface IShippinService {
    ResponseVo<Map<String, Integer>> add(Integer uid, ShippingForm form);

    ResponseVo delete(Integer uid, Integer shippingId);

    ResponseVo update(Integer uid, Integer shinppingId, ShippingForm form);

    ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize);
}
