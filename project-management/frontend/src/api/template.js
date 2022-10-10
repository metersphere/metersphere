import {get, post} from 'metersphere-frontend/src/plugins/request'

export function getIssueFieldTemplatePages(goPage, pageSize, params) {
  return post(`/field/template/issue/list/${goPage}/${pageSize}`, params);
}

export function getCaseFieldTemplatePages(goPage, pageSize, params) {
  return post(`/field/template/case/list/${goPage}/${pageSize}`, params);
}

export function deleteIssueFieldTemplateById(id) {
  return get(`/field/template/issue/delete/${id}`);
}

export function deleteCaseFieldTemplateById(id) {
  return get(`/field/template/case/delete/${id}`);
}

export function getCustomFieldTemplates(params) {
  return post('/custom/field/template/list', params);
}

export function getCustomFieldDefault(params) {
  return post('/custom/field/default', params);
}

export function getCustomFields(param) {
  return post('custom/field/list', param);
}

export function getCustomFieldRelatePages(goPage, pageSize, params) {
  return post(`/custom/field/list/relate/${goPage}/${pageSize}`, params);
}

export function modifyFieldTemplateByUrl(url, params) {
  return post(url, params);
}

export function getFieldTemplateCaseOption(projectId) {
  return get(`/field/template/case/option/${projectId}`);
}

export function getFieldTemplateIssueOption(projectId) {
  return get(`/field/template/issue/option/${projectId}`);
}

export function getApiFieldTemplatePages(goPage, pageSize, params) {
  return post(`/field/template/api/list/${goPage}/${pageSize}`, params);
}

export function deleteApiFieldTemplateById(id) {
  return get(`/field/template/api/delete/${id}`);
}

export function getFieldTemplateApiOption(projectId) {
  return get(`/field/template/api/option/${projectId}`);
}
