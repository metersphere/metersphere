import MSR from '@/api/http/index';
import { GetPluginListUrl } from '@/api/requrls/system/plugin';
import type { PluginList } from '@/models/system/plugin';
import type { TableQueryParams } from '@/models/common';

// eslint-disable-next-line import/prefer-default-export
export function getPluginList(data: TableQueryParams) {
  return MSR.post<PluginList>({ url: GetPluginListUrl, data });
}
