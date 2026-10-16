import request from '/@/utils/http/axios';
import { Coupon } from './coupon';

/**
 * 用户优惠券接口定义
 */
export interface UserCoupon {
  id: number;
  userId: number;
  couponId: number;
  status: number; // 1:未使用 2:已使用 3:已过期
  useTime?: string;
  orderId?: number;
  createTime?: string;
  updateTime?: string;
  coupon?: Coupon; // 关联的优惠券信息
}

/**
 * 获取用户优惠券列表
 * @param {number} userId 用户ID
 * @param {number} status 状态(1:未使用 2:已使用 3:已过期)
 * @returns {Promise<any>} 请求Promise对象
 */
export function getUserCoupons(userId: number, status = 1): Promise<any> {
  return request({
    url: '/api/user-coupon/list',
    method: 'get',
    params: {
      userId,
      status,
    },
  });
}

/**
 * 用户领取优惠券
 * @param {number} userId 用户ID
 * @param {number} couponId 优惠券ID
 * @returns {Promise<any>} 请求Promise对象
 */
export function receiveCoupon(userId: number, couponId: number): Promise<any> {
  return request({
    url: '/api/user-coupon/receive',
    method: 'post',
    params: {
      userId,
      couponId,
    },
  });
}

/**
 * 使用优惠券
 * @param {number} userCouponId 用户优惠券ID
 * @param {number} orderId 订单ID
 * @returns {Promise<any>} 请求Promise对象
 */
export function useCoupon(userCouponId: number, orderId: number): Promise<any> {
  return request({
    url: '/api/user-coupon/use',
    method: 'post',
    params: {
      userCouponId,
      orderId,
    },
  });
}

/**
 * 检查优惠券是否可用
 * @param {number} userCouponId 用户优惠券ID
 * @param {number} totalAmount 订单总金额
 * @returns {Promise<any>} 请求Promise对象
 */
export function checkCouponAvailable(userCouponId: number, totalAmount: number): Promise<any> {
  return request({
    url: '/api/user-coupon/check',
    method: 'get',
    params: {
      userCouponId,
      totalAmount,
    },
  });
}

/**
 * 获取用户即将过期的优惠券
 * @param {number} userId 用户ID
 * @param {number} days 天数，获取多少天内即将过期的优惠券，默认7天
 * @returns {Promise<any>} 请求Promise对象
 */
export function getSoonExpiredCoupons(userId: number, days = 7): Promise<any> {
  return request({
    url: '/api/user-coupon/soon-expired',
    method: 'get',
    params: {
      userId,
      days,
    },
  });
}
