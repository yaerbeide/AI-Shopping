package com.gk.study.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Coupon;
import com.gk.study.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/coupon")
@Slf4j
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 获取可用优惠券列表
     */
    @GetMapping("/available")
    public APIResponse getAvailableCoupons() {
        try {
            List<Coupon> coupons = couponService.getAvailableCoupons();
            return new APIResponse(ResponeCode.SUCCESS, "查询成功", coupons);
        } catch (Exception e) {
            log.error("获取可用优惠券列表异常", e);
            return new APIResponse(ResponeCode.FAIL, "查询失败");
        }
    }

    /**
     * 分页获取优惠券列表
     */
    @GetMapping("/list")
    public APIResponse getCouponList(Integer pageNum, Integer pageSize, String keyword) {
        try {
            Page<Coupon> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();
            
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("name", keyword);
            }
            
            queryWrapper.orderByDesc("create_time");
            Page<Coupon> couponPage = couponService.page(page, queryWrapper);
            return new APIResponse(ResponeCode.SUCCESS, "查询成功", couponPage);
        } catch (Exception e) {
            log.error("分页获取优惠券列表异常", e);
            return new APIResponse(ResponeCode.FAIL, "查询失败");
        }
    }

    /**
     * 根据ID获取优惠券详情
     */
    @GetMapping("/detail")
    public APIResponse getCouponDetail(Long id) {
        try {
            Coupon coupon = couponService.getCouponById(id);
            if (coupon == null) {
                return new APIResponse(ResponeCode.FAIL, "优惠券不存在");
            }
            return new APIResponse(ResponeCode.SUCCESS, "查询成功", coupon);
        } catch (Exception e) {
            log.error("获取优惠券详情异常", e);
            return new APIResponse(ResponeCode.FAIL, "查询失败");
        }
    }

    /**
     * 添加优惠券
     */
    @PostMapping("/add")
    public APIResponse addCoupon(@RequestBody Coupon coupon) {
        try {
            boolean result = couponService.addCoupon(coupon);
            if (result) {
                return new APIResponse(ResponeCode.SUCCESS, "添加成功");
            } else {
                return new APIResponse(ResponeCode.FAIL, "添加失败");
            }
        } catch (Exception e) {
            log.error("添加优惠券异常", e);
            return new APIResponse(ResponeCode.FAIL, "添加失败");
        }
    }

    /**
     * 更新优惠券
     */
    @PostMapping("/update")
    public APIResponse updateCoupon(@RequestBody Coupon coupon) {
        try {
            boolean result = couponService.updateCoupon(coupon);
            if (result) {
                return new APIResponse(ResponeCode.SUCCESS, "更新成功");
            } else {
                return new APIResponse(ResponeCode.FAIL, "更新失败");
            }
        } catch (Exception e) {
            log.error("更新优惠券异常", e);
            return new APIResponse(ResponeCode.FAIL, "更新失败");
        }
    }

    /**
     * 删除优惠券
     */
    @PostMapping("/delete")
    public APIResponse deleteCoupon(@RequestParam Long id) {
        try {
            boolean result = couponService.deleteCoupon(id);
            if (result) {
                return new APIResponse(ResponeCode.SUCCESS, "删除成功");
            } else {
                return new APIResponse(ResponeCode.FAIL, "删除失败");
            }
        } catch (Exception e) {
            log.error("删除优惠券异常", e);
            return new APIResponse(ResponeCode.FAIL, "删除失败");
        }
    }
} 