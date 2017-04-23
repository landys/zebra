package com.yiran.xingtian.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(exclude={"id", "createdAt", "updatedAt"})
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long uid;
    /**
     * Long appId for old, Integer payAppId for new.
     */
    private Integer payAppId;
    private Long mallId;
    private Long groupId;
    private Long groupOrderId;
    private String orderSn;
    private Integer status;
    private Integer orderStatus;
    private Integer payStatus;
    private Integer shippingStatus;
    private Integer orderTime;
    private Integer payTime;
    private Integer confirmTime;
    private Integer shippingTime;
    private Integer receiveTime;
    private Integer goodsAmount;
    private Integer shippingAmount;
    private Integer discountAmount;
    private Integer orderAmount;
    private String receiveName;
    private String mobile;
    private Integer provinceId;
    private String provinceName;
    private Integer cityId;
    private String cityName;
    private Integer districtId;
    private String districtName;
    private String shippingAddress;
    private Integer addressType;
    private Integer shippingId;
    private String trackingNumber;
    private String idCardNo;
    private String idCardName;
    /**
     * Boolean isLucky for old, Integer luckyStatus for new.
     */
    private Integer luckyStatus;
    private Boolean isWms;
    /**
     * isComment for old, commentCount for new.
     */
    private Integer commentCount;
    private Integer createdAt;
    private Integer updatedAt;
    /**
     * Only for new db.
     */
    private Integer version;
    /**
     * Only for new db.
     */
    private Boolean isDeleted;

}