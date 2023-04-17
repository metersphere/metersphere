import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/ui/scenario/module/';

export function uiScenarioModuleProjectList(projectId) {
  return get(BASE_URL + `list/${projectId}`);
}

export function getUiScenarioModuleListByCondition(projectId, type, params) {
  return post(BASE_URL + `list/${projectId}/${type}`, params);
}
