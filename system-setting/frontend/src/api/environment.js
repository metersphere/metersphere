import {get, post, request} from 'metersphere-frontend/src/plugins/request';

export function getEnvironmentPages(goPage, pageSize, param) {
  return post(`/environment/list/${goPage}/${pageSize}`, param);
}

export function getEnvironments(projectId) {
  return get(`/environment/list/${projectId}`);
}

export function delEnvironmentById(envId) {
  return get(`/environment/delete/${envId}`);
}

export function addEnvironment(param) {
  let formData = new FormData();
  formData.append('request', new Blob([JSON.stringify(param)], {type: 'application/json'}));
  let config = {
    method: 'POST',
    url: '/environment/add',
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };  return request(config);
}

export function getEnvironmentGroupPages(goPage, pageSize, param) {
  return post(`/environment/group/list/${goPage}/${pageSize}`, param);
}

export function getAllEnvironmentGroups(param) {
  return post('/environment/group/get/all', param);
}

export function batchAddEnvGroup(param) {
  return post('/environment/group/batch/add', param);
}

export function copyEnvironmentGroup(groupId) {
  return get(`/environment/group/copy/${groupId}`);
}

export function delEnvironmentGroup(groupId) {
  return get(`/environment/group/delete/${groupId}`);
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
