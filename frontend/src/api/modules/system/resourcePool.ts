import MSR from '@/api/http/index';
import { PoolListUrl, UpdatePoolUrl, AddPoolUrl, DetailPoolUrl } from '@/api/requrls/system/resourcePool';
import type { ResourcePoolItem, ResourcePoolInfo } from '@/models/system/resourcePool';
import type { TableQueryParams } from '@/models/common';

// 获取资源池列表
export function getPoolList(data: TableQueryParams) {
  return MSR.post<ResourcePoolItem[]>({ url: PoolListUrl, data });
}

// 更新资源池信息
export function updatePoolInfo(data: ResourcePoolItem) {
  return MSR.post({ url: UpdatePoolUrl, data });
}

// 添加资源池
export function addPool(data: ResourcePoolInfo) {
  return MSR.post({ url: AddPoolUrl, data });
}

// 获取资源池详情
export function getPoolInfo(poolId: string) {
  return MSR.get<ResourcePoolItem>({ url: DetailPoolUrl, params: poolId });
}
