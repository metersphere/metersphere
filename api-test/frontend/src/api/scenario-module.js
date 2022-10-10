import {post, get} from 'metersphere-frontend/src/plugins/request'

export function getModuleByProjectId(projectId) {
  let url = '/api/automation/module/list/' + projectId;
  return get(url);
}

export function getModuleByRelevanceProjectId(relevanceProjectId) {
  let url = '/api/automation/module/list/' + relevanceProjectId;
  return get(url);
}

export function getModuleByTrash(projectId) {
  let url = '/api/automation/module/trash/list/' + projectId;
  return get(url);
}

export function editScenarioModule(params) {
  return post('/api/automation/module/edit', params);
}

export function addScenarioModule(params) {
  return post('/api/automation/module/add', params);
}

export function delScenarioModule(params) {
  return post('/api/automation/module/delete', params);
}

export function dragScenarioModule(params) {
  return post('/api/automation/module/drag', params);
}

export function posScenarioModule(params) {
  return post('/api/automation/module/pos', params);
}
