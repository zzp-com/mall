package com.zzp.mall.service.impl;

import com.zzp.mall.MallApplicationTests;
import com.zzp.mall.form.ShippingForm;
import com.zzp.mall.service.IShippinService;
import com.zzp.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ShippingServiceImplTest extends MallApplicationTests {

    @Autowired
    private IShippinService shippinService;

    @Test
    public void add() {
        ShippingForm form = new ShippingForm();
        form.setReceiverAddress("五栋二单元");
        form.setReceiverCity("岑巩县");
        form.setReceiverDistrict("幸福一号");
        form.setReceiverMobile("123");
        form.setReceiverName("zzp");
        form.setReceiverProvince("贵州省");
        form.setReceiverZip("557809");
        ResponseVo response = shippinService.add(1, form);
        log.info("response = {}", response);
    }

    @Test
    public void delete() {
        ResponseVo responseVo = shippinService.delete(1, 5);
    }

    @Test
    public void update() {
    }

    @Test
    public void list() {
        ResponseVo responseVo = shippinService.list(1, 1, 10);
        log.info("response = {}", responseVo);
    }
}