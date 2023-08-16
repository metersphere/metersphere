import MSR from '@/api/http/index';
import {
  PoolListUrl,
  UpdatePoolUrl,
  AddPoolUrl,
  DetailPoolUrl,
  DeletePoolUrl,
  EnablePoolUrl,
} from '@/api/requrls/setting/resourcePool';

import type { LocationQueryValue } from 'vue-router';
import type {
  ResourcePoolItem,
  AddResourcePoolParams,
  UpdateResourcePoolParams,
  ResourcePoolDetail,
} from '@/models/setting/resourcePool';
import type { CommonList, TableQueryParams } from '@/models/common';

// 获取资源池列表
export function getPoolList(data: TableQueryParams) {
  return MSR.post<CommonList<ResourcePoolItem>>({ url: PoolListUrl, data });
}

// 更新资源池信息
export function updatePoolInfo(data: UpdateResourcePoolParams) {
  return MSR.post({ url: UpdatePoolUrl, data });
}

// 添加资源池
export function addPool(data: AddResourcePoolParams) {
  return MSR.post({ url: AddPoolUrl, data });
}

// 获取资源池详情
export function getPoolInfo(poolId: LocationQueryValue | LocationQueryValue[]) {
  return MSR.get<ResourcePoolDetail>({ url: DetailPoolUrl, params: poolId });
}

// 删除资源池
export function delPoolInfo(poolId: string) {
  return MSR.get({ url: DeletePoolUrl, params: poolId });
}

// 启用/禁用资源池
export function togglePoolStatus(poolId: string) {
  return MSR.post({ url: EnablePoolUrl, params: poolId });
}
