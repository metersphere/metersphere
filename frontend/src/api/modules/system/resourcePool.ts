import MSR from '@/api/http/index';
import { PoolListUrl, UpdatePoolUrl } from '@/api/requrls/system/resourcePool';
import type { ResourcePoolItem } from '@/models/system/resourcePool';
import type { TableQueryParams } from '@/models/common';

export function getPoolList(data: TableQueryParams) {
  return MSR.post<ResourcePoolItem[]>({ url: PoolListUrl, data });
}

export function updatePoolInfo(data: ResourcePoolItem) {
  return MSR.post({ url: UpdatePoolUrl, data });
}
