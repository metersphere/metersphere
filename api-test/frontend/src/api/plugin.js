import {get, post} from "metersphere-frontend/src/plugins/request"

export function getPlugin(id) {
  return get('/plugin/get/' + id);
}

export function getPluginList() {
  return get('/plugin/list');
}

export function customMethod(params) {
  return get('/plugin/custom/method', params);
}
