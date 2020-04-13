package com.zzp.mall.service.impl;

import com.zzp.mall.MallApplicationTests;
import com.zzp.mall.enums.ResponseEnum;
import com.zzp.mall.enums.RoleEnum;
import com.zzp.mall.pojo.User;
import com.zzp.mall.service.IUserService;
import com.zzp.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

//在单测中使用Transactional注解，数据不会存在数据库，起到回滚作用
@Transactional
public class UserServiceImplTest extends MallApplicationTests {

    @Autowired(required = false)
    private IUserService userService;

    @Test
    public void register() {
     User user =  new User("zzp","1234","111@qq.com");
     user.setRole(RoleEnum.CUSTOMER.getCode());
     userService.register(user);
    }

    @Test
    public void login() {
        register();
        ResponseVo<User> responseVo =  userService.login("zzp","1234");
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(),responseVo.getStatus());

    }
}