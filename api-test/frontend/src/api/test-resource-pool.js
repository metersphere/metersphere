import { get } from 'metersphere-frontend/src/plugins/request';

export function getTestResourcePools() {
  let url = '/testresourcepool/list/quota/valid';
  return get(url);
}
