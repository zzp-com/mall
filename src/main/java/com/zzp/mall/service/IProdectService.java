package com.zzp.mall.service;

import com.github.pagehelper.PageInfo;
import com.zzp.mall.vo.ProductDetailVo;
import com.zzp.mall.vo.ResponseVo;

public interface IProdectService {

    ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize);

    ResponseVo<ProductDetailVo> detail(Integer productId);
}
