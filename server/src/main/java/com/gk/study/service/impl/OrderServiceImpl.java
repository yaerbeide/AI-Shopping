package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.entity.Order;
import com.gk.study.service.OrderService;
import com.gk.study.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    OrderMapper mapper;

    @Override
    public List<Order> getOrderList() {
        return mapper.getList();
    }

    @Override
    public void createOrder(Order order) {
        long ct = System.currentTimeMillis();
        order.setOrderTime(String.valueOf(ct));
        //order.setOrderNumber(String.valueOf(ct));
        order.setStatus("1");
        mapper.insert(order);
    }

    @Override
    public void payOrder(Order order) {
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_number",order.getOrderNumber());
        order.setStatus("2");
        mapper.update(order,updateWrapper);
    }
    @Override
    public void deleteOrder(String id) {
        mapper.deleteById(id);
    }

    @Override
    public void updateOrder(Order order) {
        mapper.updateById(order);
    }

    @Override
    public List<Order> getUserOrderList(String userId, String status) {
        return mapper.getUserOrderList(userId, status);
//        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_id", userId);
//        if (StringUtils.isNotBlank(status)) {
//            queryWrapper.eq("status", status);
//        }
//        queryWrapper.orderBy(true, false, "order_time");
//        return mapper.selectList(queryWrapper);
    }

    @Override
    public Long createOrderAndGetId(Order order) {
        long ct = System.currentTimeMillis();
        order.setOrderTime(String.valueOf(ct));
        order.setStatus("1");
        // 插入订单并获取生成的ID
        mapper.insert(order);
        return order.getId();
    }
}
