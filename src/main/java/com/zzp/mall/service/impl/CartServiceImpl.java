package com.zzp.mall.service.impl;

import com.google.gson.Gson;
import com.mysql.cj.util.StringUtils;
import com.zzp.mall.dao.ProductMapper;
import com.zzp.mall.enums.ProductStatusEnum;
import com.zzp.mall.enums.ResponseEnum;
import com.zzp.mall.form.CartAddForm;
import com.zzp.mall.form.CartUpdateForm;
import com.zzp.mall.pojo.Cart;
import com.zzp.mall.pojo.Product;
import com.zzp.mall.service.ICartService;
import com.zzp.mall.vo.CartProductVo;
import com.zzp.mall.vo.CartVo;
import com.zzp.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private ProductMapper productMapper;


    @Autowired
    private StringRedisTemplate redisTemplate;

    private Gson gson = new Gson();

    private final static String CART_REDIS_KEY_TEMPLATE = "cart_%d";

    @Override
    public ResponseVo<CartVo> add(Integer uid, CartAddForm form) {
        int quantity = 1;

        //商品是否存在
        Product product = productMapper.selectByPrimaryKey(form.getProductId());

        if (product == null) {
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXISTS);
        }

        //商品是否正常在售
        if (!product.getStatus().equals(ProductStatusEnum.ON_SALE.getCode())) {
            return ResponseVo.error(ResponseEnum.OFF_SALE_DELETE);

        }


        //商品库存是否充足
        if (product.getStock() <= 0) {
            return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR);
        }

        //写入redis,使用hashSet

        //key:cart_1 -----使用List或者String类型，修改其中某个数据时需要遍历，太过于麻烦
//        redisTemplate.opsForValue().set(String.format(CART_REDIS_KEY_TEMPLATE , uid),
//                gson.toJson(new Cart(product.getId(),quantity,form.getSelected())));


        //使用Hash时，修改数据时，不需要遍历
        HashOperations<String, String, String> operations = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        String value = operations.get(redisKey, String.valueOf(product.getId()));
        Cart cart;
        if (StringUtils.isNullOrEmpty(value)) {
            //没有该商品，增加
            cart = new Cart(product.getId(), quantity, form.getSelected());
        } else {
            //已经有了，数量加一
            cart = gson.fromJson(value, Cart.class);
            cart.setQuantity(cart.getQuantity() + quantity);
        }

        operations.put(redisKey,
                String.valueOf(product.getId()),
                gson.toJson(cart));


        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> list(Integer uid) {
        HashOperations<String, String, String> operations = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        Map<String, String> map = operations.entries(redisKey);
        List<Integer> list = new ArrayList<>();
        Map<Integer, Object> cartMap = new HashMap(16);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            int productId = Integer.valueOf(entry.getKey());
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);
            //TODO 需要优化
            list.add(productId);
            cartMap.put(productId, cart);
        }
        List<Product> productsList = productMapper.selectByProductIdList(list);

        AtomicBoolean selectAll = new AtomicBoolean(true);
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = new ArrayList<>();

        AtomicInteger cartTotalQuantity = new AtomicInteger();
        final BigDecimal[] cartTotalPrice = {BigDecimal.ZERO};
        productsList.forEach(e -> {
            if (e != null) {
                Cart cart = (Cart) cartMap.get(e.getId());
                CartProductVo cartProductVo = new CartProductVo(
                        e.getId(), cart.getQuantity(), e.getName(),
                        e.getSubtitle(), e.getMainImage(),
                        e.getPrice(), e.getStatus(),
                        e.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
                        e.getStock(),
                        cart.getProductSelected()
                );
                cartProductVoList.add(cartProductVo);
                if (!cart.getProductSelected()) {
                    selectAll.set(false);
                }
                //计算总价（只计算选中的）
                if (cart.getProductSelected()) {
                    cartTotalPrice[0] = cartTotalPrice[0].add(cartProductVo.getProductTotalPrice());
                }

                cartTotalQuantity.addAndGet(cart.getQuantity());
            }
        });
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice[0]);
        cartVo.setSelectedAll(selectAll.get());
        cartVo.setCartTotalQuantity(cartTotalQuantity.get());
        return ResponseVo.successByData(cartVo);
    }

    @Override
    public ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm cartUpdateForm) {

        Cart cart;
        HashOperations<String, String, String> operations = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        String value = operations.get(redisKey, String.valueOf(productId));
        if (StringUtils.isNullOrEmpty(value)) {
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }
        //修改内容
        cart = gson.fromJson(value, Cart.class);
        if (cartUpdateForm.getQuantity() != null && cartUpdateForm.getQuantity() >= 0) {
            cart.setQuantity(cartUpdateForm.getQuantity());
        }
        if (cartUpdateForm.getSelected() != null) {
            cart.setProductSelected(cartUpdateForm.getSelected());
        }
        operations.put(redisKey, String.valueOf(productId), gson.toJson(cart));


        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> delete(Integer uid, Integer productId) {
        Cart cart;
        HashOperations<String, String, String> operations = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        String value = operations.get(redisKey, String.valueOf(productId));
        if (StringUtils.isNullOrEmpty(value)) {
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }
        operations.delete(redisKey, String.valueOf(productId));

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> selectAll(Integer uid) {
        HashOperations<String, String, String> operations = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        List<Cart> cartList = this.listForCar(uid);
        for (Cart cart : cartList) {
            cart.setProductSelected(true);
            operations.put(redisKey, String.valueOf(cart.getProductId()), gson.toJson(cart));
        }
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> unSelectAll(Integer uid) {
        HashOperations<String, String, String> operations = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        List<Cart> cartList = this.listForCar(uid);
        for (Cart cart : cartList) {
            cart.setProductSelected(false);
            operations.put(redisKey, String.valueOf(cart.getProductId()), gson.toJson(cart));
        }
        return list(uid);
    }

    @Override
    public ResponseVo<Integer> sum(Integer uid) {
        int sum = listForCar(uid).stream()
                .map(Cart::getQuantity)
                .reduce(0, Integer::sum);
        return ResponseVo.successByData(sum);
    }

    public List<Cart> listForCar(Integer uid) {
        HashOperations<String, String, String> operations = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        Map<String, String> map = operations.entries(redisKey);
        List<Cart> list = new ArrayList<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);
            list.add(cart);

        }
        return list;
    }

}
