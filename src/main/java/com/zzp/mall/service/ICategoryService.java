package com.zzp.mall.service;

import com.zzp.mall.vo.CategoryVo;
import com.zzp.mall.vo.ResponseVo;

import java.util.List;
import java.util.Set;

public interface ICategoryService {
    ResponseVo<List<CategoryVo>> selectAll();

    void findSubCategoryId(Integer id, Set<Integer> set);
}
