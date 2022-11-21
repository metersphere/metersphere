import { post, get } from 'metersphere-frontend/src/plugins/request';

export function apiTestReRun(condition) {
  return post('/api/test/exec/rerun', condition);
}

export function testDataGenerator(params) {
  return post('/api/test/data/generator', params);
}

export function updateRuleRelation(id, params) {
  return post('/api/update/rule/relation/add/' + id, params);
}

export function synCaseBatch(params) {
  return post('/api/sync/case/batch', params);
}

export function relationGet(id, type) {
  return get('/api/update/rule/relation/get/' + id + '/' + type);
}

export function getProjectVersions(projectId) {
  return get('/project/version/get-project-versions/' + projectId);
}

export function versionEnableByProjectId(projectId) {
  return get('/project/version/enable/' + projectId);
}
