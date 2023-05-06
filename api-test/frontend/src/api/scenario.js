import { fileUpload } from '@/api/base-network';
import { get, post, request } from 'metersphere-frontend/src/plugins/request';

export function getScenarioById(scenarioId) {
  return get('/api/automation/get/' + scenarioId);
}

export function getScenarioWithBLOBsById(scenarioId) {
  return get('/api/automation/scenario-details/' + scenarioId);
}

export function getScenarioList(currentPage, pageSize, condition) {
  let url = '/api/automation/list/' + currentPage + '/' + pageSize;
  return post(url, condition);
}

export function getScheduleDetail(scenarioIds) {
  let url = '/api/automation/scenario/schedule';
  return post(url, scenarioIds);
}

export function getScenarioByTrash(condition) {
  return post('/api/automation/list/all/trash', condition);
}

export function getScenarioByProjectId(projectId) {
  return get('/api/automation/env-project-ids/' + projectId);
}

export function checkScenarioEnv(scenarioId) {
  return get('/api/automation/env-valid/' + scenarioId);
}

export function execStop(reportId) {
  return get('/api/automation/stop/' + reportId);
}

export function execBatchStop(res) {
  return post('/api/automation/stop/batch', res);
}

export function getFollowByScenarioId(scenarioId) {
  return get('/api/automation/follow/' + scenarioId);
}

export function getScenarioVersions(scenarioId) {
  return get('/api/automation/versions/' + scenarioId);
}

export function delByScenarioId(scenarioId) {
  return get('/api/automation/delete/' + scenarioId);
}

export function delByScenarioIdAndRefId(scenarioId, refId) {
  return get('/api/automation/delete/' + scenarioId + '/' + refId);
}

export function deleteBatchByCondition(params) {
  return post('/api/automation/del-batch', params);
}

export function apiScenarioAll(params) {
  return post('/api/automation/id/all', params);
}

export function getApiScenarios(params) {
  return post('/api/automation/get-scenario-list', params);
}

export function getApiScenarioStep(params) {
  return post('/api/automation/get-scenario-step', params);
}

export function genPerformanceTestJmx(params) {
  return post('/api/automation/gen-jmx', params);
}

export function apiScenarioEnv(params) {
  return post('/api/automation/env', params);
}

export function apiScenarioEnvMap(params) {
  return post('/api/automation/env/map', params);
}

export function getApiScenarioProjectIdByConditions(params) {
  return post('/api/automation/list-project-ids', params);
}

export function createSchedule(params) {
  return post('/api/automation/schedule/create', params);
}

export function updateSchedule(params) {
  return post('/api/automation/schedule/update', params);
}

export function setScenarioDomain(params) {
  return post('/api/automation/set-domain', params);
}

export function listWithIds(params) {
  return post('/api/automation/list-blobs', params);
}

export function getApiScenarioEnv(params) {
  return post('/api/automation/scenario-env', params);
}

export function batchEditScenario(params) {
  return post('/api/automation/batch/edit', params);
}

export function batchCopyScenario(params) {
  return post('/api/automation/batch/copy', params);
}

export function updateScenarioEnv(params) {
  return post('/api/automation/batch/update/env', params);
}

export function scenarioPlan(params) {
  return post('/api/automation/scenario/plan', params);
}

export function runBatch(params) {
  return post('/api/automation/run/batch', params);
}

export function scenarioRun(params) {
  return post('/api/automation/run', params);
}

export function scenarioReduction(params) {
  return post('/api/automation/reduction', params);
}

export function scenarioAllIds(params) {
  return post('/api/automation/id/all', params);
}

export function checkBeforeDelete(params) {
  return post('/api/automation/get-del-details', params);
}

export function removeScenarioToGcByBatch(params) {
  return post('/api/automation/move-gc-batch', params);
}

export function exportScenario(params) {
  return post('/api/automation/export', params);
}

export function batchGenPerformanceTestJmx(params) {
  return post('/api/automation/gen-jmx-batch', params);
}

export function updateScenarioFollows(id, params) {
  return post('/api/automation/update/follows/' + id, params);
}

export function importScenario(url, file, files, params) {
  return fileUpload(url, file, files, params);
}

export function editApiScenarioCaseOrder(request) {
  return post('/api/automation/sort', request);
}

export function editScenario(config) {
  return request(config);
}
