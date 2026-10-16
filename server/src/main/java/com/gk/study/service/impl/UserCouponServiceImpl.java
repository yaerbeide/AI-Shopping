package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.entity.Coupon;
import com.gk.study.entity.UserCoupon;
import com.gk.study.mapper.UserCouponMapper;
import com.gk.study.service.CouponService;
import com.gk.study.service.UserCouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    private static final Logger log = LoggerFactory.getLogger(UserCouponServiceImpl.class);

    @Autowired
    private CouponService couponService;

    @Override
    public List<UserCoupon> getUserCoupons(Long userId, Integer status) {
        return baseMapper.getUserCoupons(userId, status);
    }
    
    @Override
    public List<UserCoupon> getCoupons(Long userId) {
        return baseMapper.getCoupons(userId);
    }

    @Override
    @Transactional
    public boolean receiveCoupon(Long userId, Long couponId) {
        // 检查优惠券是否存在且可用
        Coupon coupon = couponService.getCouponById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            return false;
        }

        // 检查是否在有效期内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            return false;
        }

        // 检查用户是否已领取此优惠券
        QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("coupon_id", couponId);
        if (count(queryWrapper) > 0) {
            return false; // 已经领取过
        }

        // 保存用户优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(1); // 1:未使用
        userCoupon.setCreateTime(LocalDateTime.now());
        return save(userCoupon);
    }

    @Override
    @Transactional
    public boolean useCoupon(Long userCouponId, Long orderId) {
        try {
            // 首先检查用户优惠券是否存在
            UserCoupon userCoupon = getById(userCouponId);
            if (userCoupon == null) {
                log.error("使用优惠券失败：用户优惠券不存在，userCouponId={}", userCouponId);
                return false;
            }
            
            // 检查优惠券是否已被使用
            if (userCoupon.getStatus() != 1) {
                log.error("使用优惠券失败：优惠券状态不是未使用，userCouponId={}, status={}", userCouponId, userCoupon.getStatus());
                return false;
            }
            
            // 直接更新实体对象
            userCoupon.setStatus(2); // 设置为已使用
            userCoupon.setUsedTime(LocalDateTime.now());
            userCoupon.setOrderId(orderId);
            userCoupon.setUpdateTime(LocalDateTime.now());
            
            boolean result = updateById(userCoupon);
            
            if (result) {
                log.info("使用优惠券成功：userCouponId={}, orderId={}", userCouponId, orderId);
            } else {
                log.error("使用优惠券失败：更新数据失败，userCouponId={}, orderId={}", userCouponId, orderId);
            }
            
            return result;
        } catch (Exception e) {
            log.error("使用优惠券异常", e);
            throw e; // 让事务回滚
        }
    }

    @Override
    public boolean checkCouponAvailable(Long userCouponId, Double totalAmount) {
        // 获取用户优惠券
        UserCoupon userCoupon = getById(userCouponId);
        if (userCoupon == null || userCoupon.getStatus() != 1) {
            return false;
        }

        // 获取优惠券详情
        Coupon coupon = couponService.getCouponById(userCoupon.getCouponId());
        if (coupon == null || coupon.getStatus() != 1) {
            return false;
        }

        // 检查是否在有效期内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            return false;
        }

        // 检查使用门槛
        if (coupon.getThreshold() != null && totalAmount < coupon.getThreshold().doubleValue()) {
            return false;
        }

        return true;
    }
    
    @Override
    public List<UserCoupon> getSoonExpiredCoupons(Long userId, int days) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 计算未来几天的日期
        LocalDateTime futureDate = now.plusDays(days);
        
        log.info("获取用户即将过期的优惠券，userId={}, days={}, now={}, futureDate={}", userId, days, now, futureDate);
        
        // 调用Mapper查询即将过期的优惠券
        // 修改查询条件，包括已经过期但在指定时间范围内的优惠券
        return baseMapper.getSoonExpiredCoupons(userId, now.minusDays(1), futureDate);
    }
}
