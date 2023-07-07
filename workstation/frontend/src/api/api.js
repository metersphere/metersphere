import {get, post} from "metersphere-frontend/src/plugins/request"

const API_URL = '/api/definition/';
const CASE_URL = '/api/testcase/';
const SYNC_URL = '/api/sync/';
/*api*/

export function getDefinitionPage(page, pageSize, params) {
  return post(API_URL+'list/' + page + '/' + pageSize, params);
}

export function batchEditByParams(params) {
  return post(API_URL+'edit-batch', params);
}

export function getApiReportDetail(id) {
  let url = API_URL+'report/get/' + id;
  return get(url);
}

export function getDefinitionById(id) {
  return get('/api/definition/get/' + id);
}


/*case*/

export function editApiTestCaseOrder(request, callback) {
  return post(CASE_URL+'edit/order', request, callback);
}

export function apiTestCasePage(page, pageSize, params) {
  return post(CASE_URL+'list/' + page + '/' + pageSize, params);
}

export function getApiCaseById(id) {
  return get(CASE_URL+'get/' + id);
}


export function getCaseById(id) {
  return get(CASE_URL+'get-details/' + id);
}

export function editApiCaseByParam(params) {
  return post(CASE_URL+'edit-batch', params);
}

export function testCaseBatchRun(params) {
  return post(CASE_URL+'batch/run', params);
}

export function getApiCaseEnvironments(param) {
  return post(CASE_URL + '/get/env', param);
}


/*sync*/

export function batchSyncCase(params) {
  return post(SYNC_URL+'case/batch', params);
}

export function batchIgnoreCase(params) {
  return post(SYNC_URL+'case/batch/ignore', params);
}

export function batchIgnoreApi(params) {
  return post(SYNC_URL+'/batch/ignore', params);
}

export function batchSyncApi(params){
  return post(SYNC_URL+'batch', params);
}

