package com.zzp.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zzp.mall.dao.OrderItemMapper;
import com.zzp.mall.dao.OrderMapper;
import com.zzp.mall.dao.ProductMapper;
import com.zzp.mall.dao.ShippingMapper;
import com.zzp.mall.enums.OrderStatusEnum;
import com.zzp.mall.enums.PaymentTypeEnum;
import com.zzp.mall.enums.ProductStatusEnum;
import com.zzp.mall.enums.ResponseEnum;
import com.zzp.mall.pojo.*;
import com.zzp.mall.service.ICartService;
import com.zzp.mall.service.IOrderService;
import com.zzp.mall.vo.OrderItemVo;
import com.zzp.mall.vo.OrderVo;
import com.zzp.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IOrderServiceImpl implements IOrderService {

    @Autowired(required = false)
    private ShippingMapper shippingMapper;

    @Autowired
    private ICartService cartService;

    @Autowired(required = false)
    private ProductMapper productMapper;

    @Autowired(required = false)
    private OrderMapper orderMapper;

    @Autowired(required = false)
    private OrderItemMapper orderItemMapper;

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public ResponseVo<OrderVo> create(Integer uid, Integer shippingId) {
        //收货地址校验
        Shipping shipping = shippingMapper.selectByUidAndShippingId(uid, shippingId);
        if (shipping == null) {
            return ResponseVo.error(ResponseEnum.SHIPPING_NOT);
        }

        //获取购物车，校验(是否有商品，库存)
        List<Cart> cartList = cartService.listForCar(uid)
                .stream().filter(Cart::getProductSelected)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cartList)) {
            return ResponseVo.error(ResponseEnum.CART_SELECTED_IS_EMPTY);
        }
        //获取cartList里的products
        Set<Integer> set = cartList.stream().map(Cart::getProductId).collect(Collectors.toSet());

        List<Product> list = productMapper.selectByProductIdList(new ArrayList<>(set));

        Map<Integer, Product> map = list.stream().collect(Collectors.toMap(Product::getId, product -> product));

        List<OrderItem> orderItems = new ArrayList<>();
        long orderNo = generateOrderNo();
        for (Cart cart : cartList) {
            //根据productId查数据库
            Product product = map.get(cart.getProductId());
            //判断商品存在
            if (product == null) {
                return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXISTS, "商品不存在, " + cart.getProductId());
            }
            //商品的上下架状态
            if (ProductStatusEnum.ON_SALE.getCode().equals(product.getStock())) {
                return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR, "商品不是在售状态, " + cart.getProductId());
            }

            //判断库存
            if (product.getStock() < cart.getQuantity()) {
                return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR, "库存不正确, " + product.getName());
            }

            OrderItem orderItem = buildOrderItem(uid, orderNo, product, cart.getQuantity());
            orderItems.add(orderItem);

            //减库存
            product.setStock(product.getStock() - cart.getQuantity());
            int row = productMapper.updateByPrimaryKeySelective(product);

            if (row <= 0) {
                return ResponseVo.error(ResponseEnum.ERROR);
            }

        }
        //计算总价格，只计算被选中的商品

        //生成订单，入库
        Order order = buildOrder(uid, orderNo, shippingId, orderItems);
        int row = orderMapper.insertSelective(order);
        if (row <= 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        row = orderItemMapper.batchInsert(orderItems);
        if (row <= 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        //更新购物车（选中的商品）,redis有事务，不能回滚
        for (Cart cart : cartList) {
            cartService.delete(uid, cart.getProductId());
        }

        //构造orderVo
        OrderVo orderVo = buidOrderVo(order, orderItems, shipping);
        return ResponseVo.successByData(orderVo);
    }

    @Override
    public ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> list = orderMapper.selectByUid(uid);

        Set<Long> orderNoSet = list.stream().map(Order::getOrderNo).collect(Collectors.toSet());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNoSet);
        Map<Long, List<OrderItem>> orderItemMap = orderItemList.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderNo));
        Set<Integer> shippingSet = list.stream().map(Order::getShippingId).collect(Collectors.toSet());

        List<Shipping> shippingList = shippingMapper.selectByIdSet(shippingSet);
        Map<Integer, Shipping> shippingMap = shippingList.stream()
                .collect(Collectors.toMap(Shipping::getId, shipping -> shipping));

        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order order : list) {
            OrderVo orderVo = buidOrderVo(order, orderItemMap.get(order.getOrderNo()), shippingMap.get(order.getShippingId()));
            orderVoList.add(orderVo);
        }
        PageInfo pageInfo = new PageInfo(list);
        pageInfo.setList(orderVoList);
        return ResponseVo.successByData(pageInfo);
    }

    @Override
    public ResponseVo<OrderVo> detail(Integer uid, Long orderNo) {

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getUserId().equals(uid)) {
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXISTS);
        }

        Set<Long> orderNoSet = new HashSet();
        orderNoSet.add(order.getOrderNo());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNoSet);

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());

        OrderVo orderVo = buidOrderVo(order, orderItemList, shipping);

        return ResponseVo.successByData(orderVo);
    }

    @Override
    public ResponseVo cancel(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getUserId().equals(uid)) {
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXISTS);
        }
        //只有未付款订单可以取消
        if (order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())) {
            return ResponseVo.error(ResponseEnum.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatusEnum.CANCELED.getCode());
        order.setCloseTime(new Date());
        int row = orderMapper.updateByPrimaryKeySelective(order);
        if (row == 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        return ResponseVo.successByMsg();
    }

    @Override
    public void paid(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null ) {
            throw  new RuntimeException(ResponseEnum.ORDER_NOT_EXISTS.getDesc()+"订单id"+orderNo);
        }

        //只有未付款订单可以变成已付款，
        if (!order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())) {
            throw  new RuntimeException(ResponseEnum.ORDER_NOT_EXISTS.getDesc()+"订单id"+orderNo);
        }
        order.setStatus(OrderStatusEnum.PAID.getCode());
        order.setPaymentTime(new Date());
       int row =  orderMapper.updateByPrimaryKeySelective(order);
       if(row <= 0){
           throw  new RuntimeException("将订单更新为已支付状态失败，订单id"+orderNo);
       }
    }

    private OrderVo buidOrderVo(Order order, List<OrderItem> orderItems, Shipping shipping) {
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(order, orderVo);

        List<OrderItemVo> list = orderItems.stream().map(e -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            BeanUtils.copyProperties(e, orderItemVo);
            return orderItemVo;
        }).collect(Collectors.toList());

        orderVo.setOrderItemVoList(list);
        if (shipping != null) {
            orderVo.setShippingId(shipping.getId());
            orderVo.setShippingVo(shipping);
        }

        return orderVo;
    }

    private Order buildOrder(int uid, long orderNo,
                             int ShippingId, List<OrderItem> orderItems) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(uid);
        order.setShippingId(ShippingId);
        BigDecimal payment = orderItems.stream().map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setPayment(payment);
        order.setPaymentType(PaymentTypeEnum.PAY_ONLONE.getCode());
        order.setPostage(0);
        order.setStatus(OrderStatusEnum.NO_PAY.getCode());
        return order;
    }

    /**
     * 企业级：分布式唯一id
     *
     * @return
     */
    private long generateOrderNo() {
        return System.currentTimeMillis() + new Random().nextInt(99);
    }

    private OrderItem buildOrderItem(Integer uid, Long orderNo, Product product, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setUserId(uid);
        item.setOrderNo(orderNo);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getMainImage());
        item.setCurrentUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}
