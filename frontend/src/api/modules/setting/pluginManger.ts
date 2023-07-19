import MSR from '@/api/http/index';
import { GetPluginListUrl } from '@/api/requrls/setting/plugin';
import type { PluginList } from '@/models/setting/plugin';
import type { TableQueryParams } from '@/models/common';

// eslint-disable-next-line import/prefer-default-export
export function getPluginList(data: TableQueryParams) {
  return MSR.post<PluginList>({ url: GetPluginListUrl, data });
}
