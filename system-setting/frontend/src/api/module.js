import {get} from "metersphere-frontend/src/plugins/request"

export function getModuleList() {
  return get('/module/list')
}

export function updateStatus(key, module) {
  return get(`/module/update/${key}/status/${module}`);
}
