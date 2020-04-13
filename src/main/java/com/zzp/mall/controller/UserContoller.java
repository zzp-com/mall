package com.zzp.mall.controller;

import com.zzp.mall.consts.MallConst;
import com.zzp.mall.form.UserLoginForm;
import com.zzp.mall.form.UserRegisterForm;
import com.zzp.mall.pojo.User;
import com.zzp.mall.service.IUserService;
import com.zzp.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@Slf4j
public class UserContoller {

    @Autowired(required = false)
    private IUserService userService;

    @PostMapping("/user/register")
    public ResponseVo register(@Valid @RequestBody UserRegisterForm userForm
                               ) {


        User user = new User();
        BeanUtils.copyProperties(userForm, user);
        return userService.register(user);
    }

    @PostMapping("/user/login")
    public ResponseVo<User> login(@Valid @RequestBody UserLoginForm loginForm,
                                  HttpSession session) {


        ResponseVo<User> responseVo = userService.login(loginForm.getUsername(), loginForm.getPassword());

        //设置session
        session.setAttribute(MallConst.CURRENT_USER, responseVo.getData());


        return responseVo;
    }


    @GetMapping("/user")
    public ResponseVo<User> userInfo(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return ResponseVo.successByData(user);
    }

    //TODO 判断登录状态
    @PostMapping("/user/logout")
    public ResponseVo logout(HttpSession session) {
        session.removeAttribute(MallConst.CURRENT_USER);
        return ResponseVo.successByMsg();
    }

}
