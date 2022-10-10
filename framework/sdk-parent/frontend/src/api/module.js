import {get} from "../plugins/request"

export function getModuleList() {
  return get('/module/list');
}

