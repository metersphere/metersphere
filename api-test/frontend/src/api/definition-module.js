import { get, post } from 'metersphere-frontend/src/plugins/request';

export function getApiModules(projectId, protocol, currentVersion) {
  let url =
    '/api/module/list/' +
    projectId +
    '/' +
    protocol +
    (currentVersion ? '/' + currentVersion : '');
  return get(url);
}

export function getApiModuleByProjectIdAndProtocol(projectId, protocol) {
  let url = '/api/module/list/' + projectId + '/' + protocol;
  return get(url);
}

export function getApiModuleByTrash(projectId, protocol, currentVersion) {
  let url =
    '/api/module/trash/list/' +
    projectId +
    '/' +
    protocol +
    '/' +
    (currentVersion ? '/' + currentVersion : '');
  return get(url);
}

export function getUserDefaultApiType() {
  let url = '/api/module/default-type';
  return get(url);
}

export function trashCount(projectId, currentProtocol) {
  let url = '/api/module/trash-count/' + projectId + '/' + currentProtocol;
  return get(url);
}

export function editModule(param) {
  return post('/api/module/edit', param);
}

export function addModule(param) {
  return post('/api/module/add', param);
}

export function delModule(param) {
  return post('/api/module/delete', param);
}

export function dragModule(param) {
  return post('/api/module/drag', param);
}

export function posModule(param) {
  return post('/api/module/pos', param);
}
