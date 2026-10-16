import { get, post } from '/@/utils/http/axios';

enum URL {
  guide = '/api/ai/guide',
  productCopy = '/api/ai/admin/product/generate',
  productImageGenerate = '/api/ai/admin/product/image/generate',
  productProfileDetail = '/api/ai/admin/product-profile/detail',
  productProfileGenerate = '/api/ai/admin/product-profile/generate',
}

const guideApi = async (data: any) => post<any>({ url: URL.guide, data, headers: {} });
const productCopyApi = async (data: any) => post<any>({ url: URL.productCopy, data, headers: {}, timeout: 60000 });
const productImageGenerateApi = async (data: any) => post<any>({ url: URL.productImageGenerate, data, headers: {}, timeout: 120000 });
const productProfileDetailApi = async (params: any) => get<any>({ url: URL.productProfileDetail, params, data: {}, headers: {} });
const productProfileGenerateApi = async (data: any) => post<any>({ url: URL.productProfileGenerate, data, headers: {}, timeout: 60000 });

export { guideApi, productCopyApi, productImageGenerateApi, productProfileDetailApi, productProfileGenerateApi };
