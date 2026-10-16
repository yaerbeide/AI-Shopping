import request from '/@/utils/http/axios';

/**
 * 优惠券类型定义
 */
export interface Coupon {
  id: number;
  name: string;
  type: number; // 1: 满减券, 2: 折扣券
  amount?: number; // 满减券金额
  discount?: number; // 折扣券折扣率 (0.1-1之间的小数)
  threshold?: number; // 使用门槛金额
  startTime: string;
  endTime: string;
  status: number; // 1: 启用, 0: 禁用
  description?: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * 分页请求参数接口
 */
interface PageParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
}

/**
 * 获取可领取的优惠券列表
 * @returns {Promise<any>} 请求Promise对象
 */
export function getAvailableCoupons(): Promise<any> {
  return request({
    url: '/api/coupon/available',
    method: 'get',
  });
}

/**
 * 获取优惠券列表
 * @param {number} pageNum 页码
 * @param {number} pageSize 每页条数
 * @param {string} keyword 搜索关键词
 * @returns {Promise<any>} 请求Promise对象
 */
export function getCouponList(pageNum = 1, pageSize = 10, keyword = ''): Promise<any> {
  const params: PageParams = {
    pageNum,
    pageSize,
    keyword,
  };

  return request({
    url: '/api/coupon/list',
    method: 'get',
    params,
  });
}

/**
 * 获取优惠券详情
 * @param {number} id 优惠券ID
 * @returns {Promise<any>} 请求Promise对象
 */
export function getCouponDetail(id: number): Promise<any> {
  return request({
    url: '/api/coupon/detail',
    method: 'get',
    params: { id },
  });
}

/**
 * 添加优惠券
 * @param {Coupon} data 优惠券数据
 * @returns {Promise<any>} 请求Promise对象
 */
export function addCoupon(data: Partial<Coupon>): Promise<any> {
  return request({
    url: '/api/coupon/add',
    method: 'post',
    data,
  });
}

/**
 * 更新优惠券
 * @param {Coupon} data 优惠券数据
 * @returns {Promise<any>} 请求Promise对象
 */
export function updateCoupon(data: Partial<Coupon>): Promise<any> {
  return request({
    url: '/api/coupon/update',
    method: 'post',
    data,
  });
}

/**
 * 删除优惠券
 * @param {number} id 优惠券ID
 * @returns {Promise<any>} 请求Promise对象
 */
export function deleteCoupon(id: number): Promise<any> {
  return request({
    url: '/api/coupon/delete',
    method: 'post',
    params: { id },
  });
}
