import { get, post } from '/@/utils/http/axios';
import { UserState } from '/@/store/modules/user/types';

enum URL {
  userCollectList = '/api/thingCollect/getUserCollectList',
  collect = '/api/thingCollect/collect',
  unCollect = '/api/thingCollect/unCollect',
}

// 收藏API需要使用FormData格式
const collectApi = async (data: any) =>
  post<any>({ url: URL.collect, params: {}, data: data, headers: { 'Content-Type': 'multipart/form-data;charset=utf-8' } });
const unCollectApi = async (params: any) => post<any>({ url: URL.unCollect, params: params });
const userCollectListApi = async (params: any) => get<any>({ url: URL.userCollectList, params: params });

export { collectApi, unCollectApi, userCollectListApi };
