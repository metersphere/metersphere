import {fileUpload} from '@/api/base-network';
import {get, post} from 'metersphere-frontend/src/plugins/request';

export function apiCountByProjectId(projectId, versionId) {
  return get('/home/api/count/' + projectId + '/' + versionId);
}

export function apiCoveredByProjectId(projectId, versionId) {
  return get('/home/api/covered/' + projectId + '/' + versionId);
}

export function scenarioCountByProjectId(projectId, versionId) {
  return get('/home/scenario/count/' + projectId + '/' + versionId);
}

export function scenarioCoveredByProjectId(projectId, versionId) {
  return get('/home/scenario/covered/' + projectId + '/' + versionId);
}

export function apiCaseCountByProjectId(projectId, versionId) {
  return get('/home/api/case/count/' + projectId + '/' + versionId);
}

export function apiCaseCoveredByProjectId(projectId, versionId) {
  return get('/home/api/case/covered/' + projectId + '/' + versionId);
}

export function scheduleTaskCountByProjectId(projectId, versionId) {
  return get('/home/schedule/task/count/' + projectId + '/' + versionId);
}

export function dubboProviders(params) {
  return post('/home/api/dubbo/providers', params);
}

export function databaseValidate(params) {
  return post('/api/database/validate', params);
}

export function genPerformanceTestXml(file, files, params) {
  let url = '/home/gen/performance/xml';
  return fileUpload(url, file, files, params);
}

export function getRunningTask(selectProjectId, versionId, currentPage, pageSize) {
  return post('/home/runningTask/' + selectProjectId + '/' + versionId + '/' + currentPage + '/' + pageSize);
}

export function formatNumber(param) {
  let num = (param || 0).toString(),
    result = '';
  while (num.length > 3) {
    result = ',' + num.slice(-3) + result;
    num = num.slice(0, num.length - 3);
  }
  if (num) {
    result = num + result;
  }
  return result;
}
