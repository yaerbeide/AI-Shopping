package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.entity.Coupon;
import com.gk.study.mapper.CouponMapper;
import com.gk.study.service.CouponService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Override
    public List<Coupon> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .le("start_time", now)
                .ge("end_time", now)
                .orderByAsc("threshold");
        return list(queryWrapper);
    }

    @Override
    public Coupon getCouponById(Long id) {
        return getById(id);
    }

    @Override
    public boolean addCoupon(Coupon coupon) {
        coupon.setCreateTime(LocalDateTime.now());
        return save(coupon);
    }

    @Override
    public boolean updateCoupon(Coupon coupon) {
        coupon.setUpdateTime(LocalDateTime.now());
        return updateById(coupon);
    }

    @Override
    public boolean deleteCoupon(Long id) {
        return removeById(id);
    }
} 