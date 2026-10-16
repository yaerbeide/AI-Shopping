package com.gk.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gk.study.entity.Coupon;

import java.util.List;

public interface CouponService extends IService<Coupon> {
    
    /**
     * 获取可用优惠券列表
     * @return 优惠券列表
     */
    List<Coupon> getAvailableCoupons();
    
    /**
     * 根据ID获取优惠券
     * @param id 优惠券ID
     * @return 优惠券
     */
    Coupon getCouponById(Long id);
    
    /**
     * 添加优惠券
     * @param coupon 优惠券信息
     * @return 是否成功
     */
    boolean addCoupon(Coupon coupon);
    
    /**
     * 更新优惠券
     * @param coupon 优惠券信息
     * @return 是否成功
     */
    boolean updateCoupon(Coupon coupon);
    
    /**
     * 删除优惠券
     * @param id 优惠券ID
     * @return 是否成功
     */
    boolean deleteCoupon(Long id);
} 