import { get } from 'metersphere-frontend/src/plugins/request';

export function getCurrentByResourceId(id) {
  return get('/api/current/user/' + id, () => {});
}
