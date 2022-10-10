import {get, post} from 'metersphere-frontend/src/plugins/request';

export function getOperatingLogPages(goPage, pageSize, param) {
  return post(`/operating/log/list/${goPage}/${pageSize}`, param);
}

export function getLogDetailById(id) {
  return get(`/operating/log/get/${id}`);
}
