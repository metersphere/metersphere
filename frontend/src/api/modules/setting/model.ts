import MSR from '@/api/http/index';
import { EditModelUrl, GetModelInfoUrl, GetModelListUrl } from '@/api/requrls/setting/model';

import { CommonList, TableQueryParams } from '@/models/common';
import { ModelSourceDTO, ModelSourceParam } from '@/models/setting/model';

// 获取模型列表
export function getModelList(data: TableQueryParams) {
  return MSR.post<CommonList<ModelSourceDTO>>({ url: GetModelListUrl, data });
}

// 更新模型信息
export function editModelInfo(data: ModelSourceParam) {
  return MSR.post({ url: EditModelUrl, data });
}

// 获取模型信息
export function getModelInfo(id: string) {
  return MSR.get({ url: `${GetModelInfoUrl}${id}` });
}
