package com.zzp.mall.service.impl;

import com.zzp.mall.dao.UserMapper;
import com.zzp.mall.enums.ResponseEnum;
import com.zzp.mall.enums.RoleEnum;
import com.zzp.mall.pojo.User;
import com.zzp.mall.service.IUserService;
import com.zzp.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static com.zzp.mall.enums.ResponseEnum.USERNAME_EXIST;
import static com.zzp.mall.enums.ResponseEnum.USERNAME_OR_PASSWORD_ERROR;


@Service
public class UserServiceImpl implements IUserService {

  @Autowired
  private UserMapper userMapper;

    /**
     * 注册
     * @param user
     */
    @Override
    public ResponseVo register(User user) {
        //username不能重复
       int countByUsername =  userMapper.countByUsername(user.getUsername());
       if (countByUsername > 0){
           //throw new RuntimeException("该用户已被注册");
           return ResponseVo.error(USERNAME_EXIST);
        }

       //email不能重复
        int countByEmail =  userMapper.countByEmail(user.getEmail());
        if (countByEmail > 0){
            //throw new RuntimeException("该邮箱已被注册");
            return ResponseVo.error(ResponseEnum.EMAIL_EXISTS);
        }

        //密码--MdD5加密(Spring自带) digest--摘要
        String password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8));
        user.setPassword(password);
        user.setRole(RoleEnum.CUSTOMER.getCode());
        //数据写入数据库
       int resultCount =  userMapper.insertSelective(user);
        if (resultCount == 0){
           // throw new RuntimeException("用户注册失败");
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        return ResponseVo.successByMsg();
    }

    @Override
    public ResponseVo<User> login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if(user == null){
            //用户不存在
            return ResponseVo.error(USERNAME_OR_PASSWORD_ERROR);
        }
        String encryption = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
       if(!user.getPassword().equalsIgnoreCase(encryption)){
           //密码错误
           return ResponseVo.error(USERNAME_OR_PASSWORD_ERROR);
       }
        user.setPassword(null);
        return ResponseVo.successByData(user);
    }
}
