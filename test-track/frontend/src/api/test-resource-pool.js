import {get} from "metersphere-frontend/src/plugins/request"

export function getTestResourcePools(id) {
  let url = '/testresourcepool/list/quota/valid' + id;
  return get(url);
}

