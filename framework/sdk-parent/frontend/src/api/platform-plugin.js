import {post, get} from "../plugins/request";
const BASE_URL = "/global/platform/plugin/";

export function getPlatformAccountInfo() {
  return get(BASE_URL + 'account/info');
}

export function validateAccountConfig(pluginId, config) {
  return post(BASE_URL + `account/validate/${pluginId}`, config);
}
