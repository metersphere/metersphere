import {get, post} from 'metersphere-frontend/src/plugins/request';

export function listAllProject() {
  return get('/project/list/all');
}

export function getProjectsByWorkspaceId(id) {
  return get(`/project/list/all/${id}`);
}

export function getProjectById(projectId) {
  return get(`/project/get/${projectId}`);
}

export function delProjectById(id) {
  return get(`/project/delete/${id}`);
}

export function getProjectPages(goPage, pageSize, param) {
  return post(`/project/list/${goPage}/${pageSize}`, param);
}

export function modifyProjectMember(member) {
  return post('/project/member/update', member);
}

export function checkThirdPlatformProject(param) {
  return post('/project/check/third/project', param);
}

export function deleteProjectById(id) {
  return get(`/project/delete/${id}`);
}


export function saveProject(project) {
  return post('/project/add', project);
}

export function modifyProject(project) {
  return post('/project/update', project);
}


export function getAllServiceIntegration() {
  return get('/service/integration/all');
}

export function getFieldTemplateCaseOption(projectId) {
  return get(projectId ? `/project/field/template/case/option/${projectId}` : `/project/field/template/case/option`);
}

export function getFieldTemplateIssueOption(projectId) {
  return get(projectId ? `/project/field/template/issue/option/${projectId}` : `/project/field/template/issue/option`);
}

export function getFieldTemplateApiOption(projectId) {
  return get(projectId ? `/project/field/template/api/option/${projectId}` : `/project/field/template/api/option`);
}

export function getproject() {
  return get('/project/list');
}
