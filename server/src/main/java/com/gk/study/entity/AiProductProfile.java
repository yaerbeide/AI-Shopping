package com.gk.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("b_ai_product_profile")
public class AiProductProfile implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("thing_id")
    private Long thingId;

    @TableField("profile_json")
    private String profileJson;

    @TableField("search_text")
    private String searchText;

    @TableField("keywords")
    private String keywords;

    @TableField("scenes")
    private String scenes;

    @TableField("target_users")
    private String targetUsers;

    @TableField("selling_points")
    private String sellingPoints;

    @TableField("specs")
    private String specs;

    @TableField("image_summary")
    private String imageSummary;

    @TableField("content_quality_score")
    private Integer contentQualityScore;

    @TableField("source_hash")
    private String sourceHash;

    @TableField("status")
    private String status;

    @TableField("error_message")
    private String errorMessage;

    @TableField("generated_at")
    private String generatedAt;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
