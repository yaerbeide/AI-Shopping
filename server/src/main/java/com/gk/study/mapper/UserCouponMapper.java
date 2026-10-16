package com.gk.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gk.study.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    
    @Select("SELECT uc.*, c.name, c.type, c.discount, c.threshold, c.amount, c.start_time, c.end_time " +
            "FROM b_user_coupon uc " +
            "LEFT JOIN b_coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND uc.status = #{status}")
    List<UserCoupon> getUserCoupons(@Param("userId") Long userId, @Param("status") Integer status);
    
    @Select("SELECT uc.*, c.name, c.type, c.discount, c.threshold, c.amount, c.start_time, c.end_time " +
            "FROM b_user_coupon uc " +
            "LEFT JOIN b_coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId}")
    List<UserCoupon> getCoupons(@Param("userId") Long userId);
    
    @Select("SELECT uc.*, c.name, c.type, c.discount, c.threshold, c.amount, c.start_time, c.end_time " +
            "FROM b_user_coupon uc " +
            "LEFT JOIN b_coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND uc.status = 1 " +
            "AND ((c.end_time >= #{now} AND c.end_time <= #{futureDate}) OR " +
            "(c.end_time < #{now} AND DATE(c.end_time) >= DATE_SUB(DATE(#{now}), INTERVAL 1 DAY)))")
    List<UserCoupon> getSoonExpiredCoupons(@Param("userId") Long userId, @Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);
}
