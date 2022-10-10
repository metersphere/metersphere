import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/api/module/';

export function apiModuleGetUserDefaultApiType() {
  return get(BASE_URL + 'getUserDefaultApiType');
}


export function apiModuleProjectList(projectId, protocol) {
  return get(BASE_URL + `list/${projectId}/${protocol}`);
}
