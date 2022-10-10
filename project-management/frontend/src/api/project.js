import {get, post} from 'metersphere-frontend/src/plugins/request'

export function listAllProject() {
  return get('/project/list/all');
}

export function getProject(id) {
  return get(`/project/get/${id}`)
}

export function saveProject(project) {
  return post('/project/add', project);
}

export function modifyProject(project) {
  return post('/project/update', project);
}

export function deleteProjectById(id) {
  return get(`/project/delete/${id}`);
}

export function getProjectMemberSize(id) {
  return get(`/project/member/size/${id}`)
}

export function getProjectById(projectId) {
  return get(`/project/get/${projectId}`);
}

export function checkThirdPlatformProject(param) {
  return post('/project/check/third/project', param);
}

export function genTcpMockPort(projectId) {
  return get(`/project/gen-tcp-mock-port/${projectId}`);
}

export function deleteProjectMember(projectId, userId) {
  return get(`/project/member/delete/${projectId}/${userId}`);
}

export function getProjectMembers(goPage, pageSize, param) {
  return post(`/project/member/list/${goPage}/${pageSize}`, param);
}

export function modifyProjectMember(param) {
  return post('/project/member/update', param);
}

export function getJiraIssueType(param) {
  return post('/project/issues/jira/issuetype', param);
}

export function getAllServiceIntegration() {
  return get('/project/service/integration/all');
}

export function getRelatedProject(params) {
  return post('/project/list/related', params);
}

export function addProjectUser(params) {
  return post('/project/member/add', params);
}

