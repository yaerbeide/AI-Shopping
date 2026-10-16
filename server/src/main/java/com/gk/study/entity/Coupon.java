package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_coupon")
public class Coupon {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer type; // 1:满减券 2:折扣券
    
    private BigDecimal discount; // 折扣率(0-1之间，如0.8表示8折)
    
    private BigDecimal threshold; // 使用门槛(满X元可用)
    
    private BigDecimal amount; // 优惠金额(满减券时为减免金额)
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer status; // 0:禁用 1:启用
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
} 