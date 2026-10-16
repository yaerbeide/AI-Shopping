package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Coupon;
import com.gk.study.entity.UserCoupon;
import com.gk.study.service.UserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user-coupon")
@Slf4j
public class UserCouponController {

    @Autowired
    private UserCouponService userCouponService;

    /**
     * 获取用户优惠券列表
     */
    @GetMapping("/list")
    public APIResponse getUserCoupons(@RequestParam Long userId, @RequestParam(defaultValue = "1") Integer status) {
        try {
            List<UserCoupon> userCoupons = null;
            if (status == -1){
                userCoupons = userCouponService.getCoupons(userId);
            } else {
                userCoupons = userCouponService.getUserCoupons(userId, status);
            }
            
            // 确保每个UserCoupon都有设置coupon属性
            for (UserCoupon userCoupon : userCoupons) {
                if (userCoupon.getName() != null) {
                    // 如果有名称等信息，则表明SQL已经关联查出优惠券信息
                    // 创建Coupon对象并设置属性
                    Coupon coupon = new Coupon();
                    coupon.setId(userCoupon.getCouponId());
                    coupon.setName(userCoupon.getName());
                    coupon.setType(userCoupon.getType());
                    coupon.setDiscount(userCoupon.getDiscount());
                    coupon.setThreshold(userCoupon.getThreshold());
                    coupon.setAmount(userCoupon.getAmount());
                    coupon.setStartTime(userCoupon.getStartTime());
                    coupon.setEndTime(userCoupon.getEndTime());
                    
                    // 设置关联的优惠券对象
                    userCoupon.setCoupon(coupon);
                }
            }
            
            // 如果是获取未使用的优惠券，检查是否有已过期但状态未更新的
            if (status == 1) {
                List<UserCoupon> validCoupons = new ArrayList<>();
                List<UserCoupon> expiredCoupons = new ArrayList<>();
                
                LocalDateTime now = LocalDateTime.now();
                for (UserCoupon userCoupon : userCoupons) {
                    // 检查优惠券是否已过期
                    if (userCoupon.getEndTime() != null && now.isAfter(userCoupon.getEndTime())) {
                        // 已过期但状态未更新，应该更新状态为已过期
                        userCoupon.setStatus(3); // 3表示已过期
                        userCouponService.updateById(userCoupon); // 更新数据库
                        expiredCoupons.add(userCoupon);
                    } else {
                        validCoupons.add(userCoupon);
                    }
                }
                
                // 返回有效的优惠券
                return new APIResponse(ResponeCode.SUCCESS, "查询成功", validCoupons);
            }
            
            return new APIResponse(ResponeCode.SUCCESS, "查询成功", userCoupons);
        } catch (Exception e) {
            log.error("获取用户优惠券列表异常", e);
            return new APIResponse(ResponeCode.FAIL, "查询失败");
        }
    }

    /**
     * 用户领取优惠券
     */
    @PostMapping("/receive")
    public APIResponse receiveCoupon(@RequestParam Long userId, @RequestParam Long couponId) {
        try {
            boolean result = userCouponService.receiveCoupon(userId, couponId);
            if (result) {
                return new APIResponse(ResponeCode.SUCCESS, "领取成功");
            } else {
                return new APIResponse(ResponeCode.FAIL, "领取失败，可能已领取或优惠券不可用");
            }
        } catch (Exception e) {
            log.error("用户领取优惠券异常", e);
            return new APIResponse(ResponeCode.FAIL, "领取失败");
        }
    }

    /**
     * 使用优惠券
     */
    @PostMapping("/use")
    public APIResponse useCoupon(@RequestParam Long userCouponId, @RequestParam Long orderId) {
        log.info("使用优惠券请求，userCouponId={}, orderId={}", userCouponId, orderId);
        
        // 参数校验
        if (userCouponId == null || userCouponId <= 0) {
            log.error("使用优惠券失败：无效的userCouponId={}", userCouponId);
            return new APIResponse(ResponeCode.FAIL, "无效的优惠券ID");
        }
        
        if (orderId == null || orderId <= 0) {
            log.error("使用优惠券失败：无效的orderId={}", orderId);
            return new APIResponse(ResponeCode.FAIL, "无效的订单ID");
        }
        
        try {
            // 先检查优惠券是否存在
            UserCoupon userCoupon = userCouponService.getById(userCouponId);
            if (userCoupon == null) {
                log.error("使用优惠券失败：用户优惠券不存在，userCouponId={}", userCouponId);
                return new APIResponse(ResponeCode.FAIL, "优惠券不存在");
            }
            
            log.info("使用优惠券，当前状态：{}", userCoupon);
            
            boolean result = userCouponService.useCoupon(userCouponId, orderId);
            if (result) {
                log.info("使用优惠券成功，userCouponId={}, orderId={}", userCouponId, orderId);
                return new APIResponse(ResponeCode.SUCCESS, "使用成功");
            } else {
                log.error("使用优惠券失败：服务返回失败，userCouponId={}, orderId={}", userCouponId, orderId);
                return new APIResponse(ResponeCode.FAIL, "使用失败，请检查优惠券是否可用");
            }
        } catch (Exception e) {
            log.error("使用优惠券异常", e);
            return new APIResponse(ResponeCode.FAIL, "使用失败：" + e.getMessage());
        }
    }

    /**
     * 检查优惠券是否可用
     */
    @GetMapping("/check")
    public APIResponse checkCouponAvailable(@RequestParam Long userCouponId, @RequestParam Double totalAmount) {
        try {
            boolean available = userCouponService.checkCouponAvailable(userCouponId, totalAmount);
            if (available) {
                return new APIResponse(ResponeCode.SUCCESS, "优惠券可用");
            } else {
                return new APIResponse(ResponeCode.FAIL, "优惠券不可用");
            }
        } catch (Exception e) {
            log.error("检查优惠券是否可用异常", e);
            return new APIResponse(ResponeCode.FAIL, "检查失败");
        }
    }
    
    /**
     * 获取用户即将过期的优惠券
     */
    @GetMapping("/soon-expired")
    public APIResponse getSoonExpiredCoupons(@RequestParam Long userId, @RequestParam(defaultValue = "7") Integer days) {
        try {
            log.info("获取用户即将过期的优惠券，userId={}, days={}", userId, days);
            
            // 参数校验
            if (userId == null || userId <= 0) {
                return new APIResponse(ResponeCode.FAIL, "无效的用户ID");
            }
            
            if (days == null || days <= 0 || days > 30) {
                days = 7; // 默认7天，最多30天
            }
            
            List<UserCoupon> soonExpiredCoupons = userCouponService.getSoonExpiredCoupons(userId, days);
            
            // 处理优惠券信息，确保每个UserCoupon都有设置coupon属性
            for (UserCoupon userCoupon : soonExpiredCoupons) {
                if (userCoupon.getName() != null) {
                    // 创建Coupon对象并设置属性
                    Coupon coupon = new Coupon();
                    coupon.setId(userCoupon.getCouponId());
                    coupon.setName(userCoupon.getName());
                    coupon.setType(userCoupon.getType());
                    coupon.setDiscount(userCoupon.getDiscount());
                    coupon.setThreshold(userCoupon.getThreshold());
                    coupon.setAmount(userCoupon.getAmount());
                    coupon.setStartTime(userCoupon.getStartTime());
                    coupon.setEndTime(userCoupon.getEndTime());
                    
                    // 设置关联的优惠券对象
                    userCoupon.setCoupon(coupon);
                }
            }
            
            return new APIResponse(ResponeCode.SUCCESS, "查询成功", soonExpiredCoupons);
        } catch (Exception e) {
            log.error("获取用户即将过期的优惠券异常", e);
            return new APIResponse(ResponeCode.FAIL, "查询失败");
        }
    }
}
