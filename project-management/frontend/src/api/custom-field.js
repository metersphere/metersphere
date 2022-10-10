import {get, post} from 'metersphere-frontend/src/plugins/request'

export function getCustomFieldPages(goPage, pageSize, param) {
  return post(`/custom/field/list/${goPage}/${pageSize}`, param);
}

export function deleteCustomField(id) {
  return get(`/custom/field/delete/${id}`);
}

export function handleResourceSave(url, param) {
  return post(url, param);
}
