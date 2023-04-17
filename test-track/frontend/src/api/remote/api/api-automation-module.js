import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/api/automation/module/';

export function apiAutomationModuleProjectList(projectId) {
  return get(BASE_URL + `list/${projectId}`);
}

export function getAutomationModuleListByParam(projectId, params) {
  return post(BASE_URL + `list/${projectId}`, params);
}

