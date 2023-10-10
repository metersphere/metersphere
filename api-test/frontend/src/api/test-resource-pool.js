import {get, post} from 'metersphere-frontend/src/plugins/request';

export function getTestResourcePools() {
  let url = '/testresourcepool/list/quota/valid';
  return get(url);
}

export function getNodeOperationInfo(request) {
  return post(`/prometheus/query/node-operation-info`, request)
}
