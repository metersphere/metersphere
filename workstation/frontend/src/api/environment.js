import {get, post} from 'metersphere-frontend/src/plugins/request';

export function getEnvironments(projectId) {
  return get(`/environment/list/${projectId}`);
}

export function delEnvironmentById(envId) {
  return get(`/environment/delete/${envId}`);
}

export function getEnvironmentGroupPages(goPage, pageSize, param) {
  return post(`/environment/group/list/${goPage}/${pageSize}`, param);
}

export function copyEnvironmentGroup(groupId) {
  return get(`/environment/group/copy/${groupId}`);
}

export function modifyEnvironmentGroup(group) {
  return post('/environment/group/modify', group);
}

export function updateEnvironmentGroup(group) {
  return post('/environment/group/update', group);
}

export function addEnvironmentGroup(group) {
  return post('/environment/group/add', group);
}

export function getEnvGroupProject(environmentId) {
  return get(`/environment/group/project/list/${environmentId}`);
}

export function environmentGetALl() {
  return post('/environment/group/get/all', {});
}

export function getEnvironmentOptions(params) {
  return post('/environment/group/get/option', params);
}
