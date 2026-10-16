package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_user_coupon")
public class UserCoupon {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("coupon_id")
    private Long couponId;
    
    // 状态:1-未使用 2-已使用 3-已过期
    private Integer status;
    
    // 映射数据库字段use_time
    @TableField("use_time")
    private LocalDateTime usedTime;
    
    @TableField("order_id")
    private Long orderId;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // 优惠券信息 - 非表字段
    @TableField(exist = false)
    private String name;
    
    @TableField(exist = false)
    private Integer type; // 1:满减券 2:折扣券
    
    @TableField(exist = false)
    private BigDecimal discount; // 折扣率
    
    @TableField(exist = false)
    private BigDecimal threshold; // 使用门槛
    
    @TableField(exist = false)
    private BigDecimal amount; // 优惠金额
    
    @TableField(exist = false)
    private LocalDateTime startTime;
    
    @TableField(exist = false)
    private LocalDateTime endTime;
    
    // 创建一个包含完整优惠券信息的字段
    @TableField(exist = false)
    private Coupon coupon;
}
