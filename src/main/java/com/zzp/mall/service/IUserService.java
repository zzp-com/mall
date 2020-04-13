package com.zzp.mall.service;

import com.zzp.mall.pojo.User;
import com.zzp.mall.vo.ResponseVo;

public interface IUserService {
    /**
     * 注册
     * @param user
     */
    ResponseVo register(User user);
    /**
     * 登录
     */
    ResponseVo<User> login(String username,String password);

}
