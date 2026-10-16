package com.gk.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gk.study.entity.UserCoupon;

import java.util.List;

public interface UserCouponService extends IService<UserCoupon> {
    
    /**
     * 获取用户的优惠券列表
     * @param userId 用户ID
     * @param status 状态(1:未使用 2:已使用 3:已过期)
     * @return 用户优惠券列表
     */
    List<UserCoupon> getUserCoupons(Long userId, Integer status);
    
    
    List<UserCoupon> getCoupons(Long userId);
    
    /**
     * 用户领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    boolean receiveCoupon(Long userId, Long couponId);
    
    /**
     * 使用优惠券
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean useCoupon(Long userCouponId, Long orderId);
    
    /**
     * 检查优惠券是否可用
     * @param userCouponId 用户优惠券ID
     * @param totalAmount 订单总金额
     * @return 是否可用
     */
    boolean checkCouponAvailable(Long userCouponId, Double totalAmount);
    
    /**
     * 获取用户即将过期的优惠券列表
     * @param userId 用户ID
     * @param days 天数，获取多少天内即将过期的优惠券
     * @return 即将过期的优惠券列表
     */
    List<UserCoupon> getSoonExpiredCoupons(Long userId, int days);
}
