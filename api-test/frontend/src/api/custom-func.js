import { get, post } from 'metersphere-frontend/src/plugins/request';

export function getFuncById(id) {
  return get('/custom/func/get/' + id);
}

export function funcList(page, pageSize, params) {
  return post('/custom/func/list/' + page + '/' + pageSize, params);
}
