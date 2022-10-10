import {get, post} from 'metersphere-frontend/src/plugins/request'

export function getErrorReportLibraryPages(goPage, pageSize, params) {
  return post(`/error/report/library/list/${goPage}/${pageSize}`, params);
}

export function deleteErrorReportLibrary(param) {
  return post('/error/report/library/delete', param);
}

export function getErrorReportLibraryById(id) {
  return get(`/error/report/library/get/${id}`);
}

export function modifyErrorReportLibrary(param) {
  return post('/error/report/library/update', param);
}

export function saveErrorReportLibrary(param) {
  return post('/error/report/library/save', param);
}
