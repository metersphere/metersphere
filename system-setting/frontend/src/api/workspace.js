import {get, post} from 'metersphere-frontend/src/plugins/request';

export function getWorkspaces() {
  return get('/workspace/list');
}

export function getWorkspaceList(goPage, pageSize, param) {
  return post(`/workspace/list/all/${goPage}/${pageSize}`, param);
}

export function delWorkspaceSpecial(workspaceId) {
  return get(`/workspace/special/delete/${workspaceId}`);
}

export function updateWorkspaceMember(param) {
  return post('/workspace/member/update', param);
}

export function addWorkspaceSpecial(param) {
  return post('/workspace/special/add', param);
}

export function updateWorkspace(param) {
  return post('/workspace/update', param);
}

export function updateWorkspaceSpecial(param) {
  return post('/workspace/special/update', param);
}

export function getGroupResource(groupId, groupType) {
  return get(`/workspace/list/resource/${groupId}/${groupType}`);
}

export function getServiceIntegration(param) {
  return post('/service/integration/type', param);
}

export function saveServiceIntegration(param) {
  return post('/service/integration/save', param);
}

export function delServiceIntegration(param) {
  return post('/service/integration/delete', param);
}

export function authServiceIntegration(workspaceId, platform) {
  return get(`/service/integration/auth/${workspaceId}/${platform}`);
}
