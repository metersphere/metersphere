import {get} from "../plugins/request";

export function getSystemBaseSetting() {
  return get('/system/base/info');
}

export function getTestResourcePools() {
  let url = '/testresourcepool/list/quota/valid';
  return get(url);
}

