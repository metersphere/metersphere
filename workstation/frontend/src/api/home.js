import {get, post} from "metersphere-frontend/src/plugins/request"

export function apiCountByProjectId(projectId) {
  return get('/home/api/count/' + projectId);
}

export function countApiCoverageByProjectId(projectId) {
  return get('/home/api/coverage/' + projectId);
}

export function apiCaseCountByProjectId(projectId) {
  return get('/home/api/case/count/' + projectId);
}

export function scheduleTaskCountByProjectId(projectId) {
  return get('/home/schedule/task/count/' + projectId);
}

export function apiRunningTask(id, params) {
  return post('/home/running/task/' + id, params);
}

export function databaseValidate(params) {
  return post('/home/database/validate', params);
}

