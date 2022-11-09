import {fileDownload, fileUpload} from "@/api/base-network";
import {get, post} from "metersphere-frontend/src/plugins/request"

export function getRelationshipCountApi(id) {
  return get('/api/definition/relationship/count/' + id);
}

export function getRelationshipApi(id, relationshipType) {
  return get('/api/definition/relationship/' + id + '/' + relationshipType);
}

export function editApiDefinitionOrder(request) {
  return post('/api/definition/sort', request);
}

export function getDefinitionById(id) {
  return get('/api/definition/get/' + id);
}

export function getDefinitionByIdAndRefId(id, refId) {
  return get('/api/definition/get/' + id + '/' + refId);
}

export function definitionFollow(id) {
  return get('/api/definition/follow/' + id);
}

export function scheduleTask(projectId) {
  return get('/api/definition/schedule/' + projectId);
}

export function getMockEnvironment(projectId) {
  return get('/api/definition/mock-environment/' + projectId);
}

export function getDefinitionVersions(id) {
  return get('/api/definition/versions/' + id);
}

export function delDefinition(id) {
  return get('/api/definition/delete/' + id);
}

export function delDefinitionByRefId(id, refId) {
  return get('/api/definition/delete/' + id + '/' + refId);
}

export function getApiDocument(id, type) {
  return get('/api/definition/document/' + id + '/' + type);
}

export function getDefinitionEnv(userId, projectId) {
  return get('/api/definition/env/get/' + userId + '/' + projectId);
}

export function citedApiScenarioCount(id) {
  return get('/api/definition/cited-scenario/' + id);
}

export function apiListBatch(params) {
  return post('/api/definition/list/batch', params);
}

export function definitionWeekList(projectId, page, pageSize) {
  return post('/api/definition/list/week/' + projectId + '/' + page + '/' + pageSize);
}

export function getRelevanceDefinitionPage(page, pageSize, params) {
  return post('/api/definition/list/relevance/' + page + '/' + pageSize, params);
}

export function getDefinitionPage(page, pageSize, params) {
  return post('/api/definition/list/' + page + '/' + pageSize, params);
}

export function createDefinitionEnv(params) {
  return post('/api/definition/env/create', params);
}

export function updateDefinitionFollows(id, params) {
  return post('/api/definition/update/follows/' + id, params);
}

export function definitionSwitch(params) {
  return post('/api/definition/schedule-switch', params);
}

export function delDefinitionSchedule(params) {
  return post('/api/definition/del-schedule', params);
}

export function getDefinitionByResourceId(params) {
  return post('/api/definition/get-resource', params);
}

export function updateDefinitionSchedule(params) {
  return post('/api/definition/schedule/update', params);
}

export function createDefinitionSchedule(params) {
  return post('/api/definition/schedule/create', params);
}

export function definitionRawToXML(params) {
  return post('/api/definition/raw-to-xml', params);
}

export function definitionReduction(params) {
  return post('/api/definition/reduction', params);
}

export function definitionRelationship(currentPage, pageSize, params) {
  return post('/api/definition/relationship/relate/' + currentPage + '/' + pageSize, params);
}

export function addRelationship(params) {
  return post('/api/definition/relationship/add', params);
}

export function jsonGenerator(params) {
  return post('/api/definition/generator', params);
}

export function getDefinitionReference(params) {
  return post('/api/definition/get-reference', params);
}

export function deleteBatchByParams(params) {
  return post('/api/definition/del-batch', params);
}

export function removeToGcByParams(params) {
  return post('/api/definition/move-gc-batch', params);
}

export function removeToGcByIds(params) {
  return post('/api/definition/move-gc', params);
}

export function batchEditByParams(params) {
  return post('/api/definition/edit-batch', params);
}

export function batchCopyByParams(params) {
  return post('/api/definition/copy-batch', params);
}

export function exportDefinition(type, params) {
  return post('/api/definition/export/' + type, params);
}

export function apiPreview(params) {
  return post('/api/definition/preview', params);
}

export function apiDebug(file, files, params) {
  let url = '/api/definition/run/debug';
  return fileUpload(url, file, files, params);
}

export function updateDefinition(url, file, files, params) {
  url = url || '/api/definition/update';
  return fileUpload(url, file, files, params);
}

export function createDefinition(file, files, params) {
  let url = '/api/definition/create';
  return fileUpload(url, file, files, params);
}

export function exportEsbTemp(name) {
  let url = '/api/definition/export-esb-template';
  return fileDownload(url, name);
}
