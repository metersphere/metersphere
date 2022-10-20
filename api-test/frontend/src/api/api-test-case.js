import {fileUpload} from "@/api/base-network";
import {get, post} from "metersphere-frontend/src/plugins/request"

export function editApiTestCaseOrder(request) {
  return post('/api/testcase/sort', request);
}

export function getCaseById(id) {
  return get('/api/testcase/get-details/' + id);
}

export function getApiCaseById(id) {
  return get('/api/testcase/get/' + id);
}

export function getApiCaseWithBLOBs(params) {
  return post('/api/testcase/list-blobs', params);
}

export function getApiCaseFollow(id) {
  return get('/api/testcase/follow/' + id);
}

export function deleteToGc(id) {
  return get('/api/testcase/move-gc/' + id);
}

export function citedScenarioCount(id) {
  return get('/api/testcase/cited-scenario/' + id);
}

export function delApiTestCase(id) {
  return get('/api/testcase/delete/' + id);
}

export function apiTestCasePage(page, pageSize, params) {
  return post('/api/testcase/list/' + page + '/' + pageSize, params);
}

export function apiTestCaseList(params) {
  return post('/api/testcase/list', params);
}

export function editApiCaseByParam(params) {
  return post('/api/testcase/edit-batch', params);
}

export function editFollowsByParam(id, params) {
  return post('/api/testcase/update/follows/' + id, params);
}

export function testCaseBatchRun(params) {
  return post('/api/testcase/batch/run', params);
}

export function updateExecuteInfo(params) {
  return post('/api/testcase/updateExecuteInfo', params);
}

export function checkDeleteData(params) {
  return post('/api/testcase/get-del-reference', params);
}

export function delCaseBatchByParam(params) {
  return post('/api/testcase/del-batch', params);
}

export function delCaseToGcByParam(params) {
  return post('/api/testcase/move-batch-gc', params);
}

export function testCaseReduction(params) {
  return post('/api/testcase/reduction', params);
}

export function createApiCase(file, files, params) {
  let url = '/api/testcase/create';
  return fileUpload(url, file, files, params);
}

export function editApiCase(url, file, files, params) {
  return fileUpload(url, file, files, params);
}
