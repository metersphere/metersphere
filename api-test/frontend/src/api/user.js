import { get } from 'metersphere-frontend/src/plugins/request';

export function getCurrentByResourceId(id) {
  console.log(id);
  return get('/api/current/user/' + id);
}

export function getProjectMember() {
  return get('/user/project/member/list');
}
