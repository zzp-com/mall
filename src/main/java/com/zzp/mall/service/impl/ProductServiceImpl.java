package com.zzp.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zzp.mall.dao.ProductMapper;
import com.zzp.mall.enums.ProductStatusEnum;
import com.zzp.mall.enums.ResponseEnum;
import com.zzp.mall.pojo.Product;
import com.zzp.mall.service.ICategoryService;
import com.zzp.mall.service.IProdectService;
import com.zzp.mall.vo.ProductDetailVo;
import com.zzp.mall.vo.ProductVo;
import com.zzp.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProdectService {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private ProductMapper productMapper;


    @Override
    public ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        Set<Integer> categoryIdSet = new HashSet<>();
        if (categoryId != null) {
            categoryService.findSubCategoryId(categoryId, categoryIdSet);
            categoryIdSet.add(categoryId);
        }
        List<Product> list = productMapper.selectByCategoryIdSet(categoryIdSet);
        List<ProductVo> productVoList = list.stream().map(e -> {
            ProductVo productVo = new ProductVo();
            BeanUtils.copyProperties(e, productVo);
            return productVo;
        }).collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo(list);
        pageInfo.setList(productVoList);
        return ResponseVo.successByData(pageInfo);
    }

    @Override
    public ResponseVo<ProductDetailVo> detail(Integer productId) {
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product.getStatus().equals(ProductStatusEnum.OFF_SALE.getCode())
                || product.getStatus().equals(ProductStatusEnum.DELETE.getCode())) {
            return ResponseVo.error(ResponseEnum.OFF_SALE_DELETE);
        }
        ProductDetailVo productDetailVo = new ProductDetailVo();
        BeanUtils.copyProperties(product, productDetailVo);
        return ResponseVo.successByData(productDetailVo);
    }
}
