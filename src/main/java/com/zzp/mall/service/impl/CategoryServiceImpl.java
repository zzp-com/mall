package com.zzp.mall.service.impl;

import com.zzp.mall.dao.CategoryMapper;
import com.zzp.mall.pojo.Category;
import com.zzp.mall.service.ICategoryService;
import com.zzp.mall.vo.CategoryVo;
import com.zzp.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zzp.mall.consts.MallConst.ROOT_PARENT_ID;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    @Override
    public ResponseVo<List<CategoryVo>> selectAll() {
        List<Category> list = categoryMapper.selectAll();

        //查出parent_id=0
        List<CategoryVo> listVo = list.stream().filter(e -> e.getParentId().equals(ROOT_PARENT_ID))
                .map(this::categoryToConvertCategoryVo)
                .sorted(Comparator.comparing(CategoryVo::getSortOrder).reversed())
                .collect(Collectors.toList());
        //查询子项目
        findSubCategory(listVo, list);
        return ResponseVo.successByData(listVo);
    }

    @Override
    public void findSubCategoryId(Integer id, Set<Integer> set) {
        List<Category> list = categoryMapper.selectAll();
        findSubCategoryId(id,set,list);
    }


    private void findSubCategoryId(Integer id, Set<Integer> set,List<Category> list){
        for (Category category : list) {
            if(category.getParentId().equals(id)){
                set.add(category.getId());
                findSubCategoryId(category.getId(),set,list);
            }
        }
    }

    private void findSubCategory(List<CategoryVo> listVo, List<Category> list) {
        for (CategoryVo categoryVo : listVo) {
            List<CategoryVo> subCategoryVoList = new ArrayList<>();
            for (Category category : list) {
                //查询到内容，继续往下查
                if (categoryVo.getId().equals(category.getParentId())) {
                    CategoryVo categoryVo1 = categoryToConvertCategoryVo(category);
                    subCategoryVoList.add(categoryVo1);
                    subCategoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());
                    findSubCategory(subCategoryVoList, list);
                }

                categoryVo.setSubCategories(subCategoryVoList);

            }

        }
    }


    //转换数据
    private CategoryVo categoryToConvertCategoryVo(Category category) {
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category, categoryVo);
        return categoryVo;
    }

}
