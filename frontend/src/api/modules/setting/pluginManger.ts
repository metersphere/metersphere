import MSR from '@/api/http/index';
import {
  GetPluginListUrl,
  UploadPluginUrl,
  UpdatePluginUrl,
  DeletePluginUrl,
  GetScriptUrl,
} from '@/api/requrls/setting/plugin';
import type { PluginList, PluginItem, AddReqData, UpdatePluginModel, UploadFile } from '@/models/setting/plugin';

export function getPluginList() {
  return MSR.get<PluginList>({ url: GetPluginListUrl });
}
export function addPlugin(data: UploadFile) {
  return MSR.uploadFile<AddReqData>({ url: UploadPluginUrl }, data);
}
export function updatePlugin(data: UpdatePluginModel) {
  return MSR.post<PluginItem>({ url: UpdatePluginUrl, data });
}
export function deletePluginReq(id: string) {
  return MSR.get<PluginItem>({ url: DeletePluginUrl, params: id });
}
export function getScriptDetail(pluginId: string, scriptId: string) {
  return MSR.get({ url: GetScriptUrl, params: `${pluginId}/${scriptId}` });
}
