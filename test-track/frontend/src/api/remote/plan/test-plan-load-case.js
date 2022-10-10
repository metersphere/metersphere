import {post, get} from "@/business/utils/sdk-utils";
import {buildListPagePath, buildPagePath} from "@/api/base-network";

const BASE_URL = '/test/plan/load/case/';

export function testPlanLoadList(page, param) {
  return post(BASE_URL + buildListPagePath(page), param);
}

export function testPlanLoadRelevanceList(page, param) {
  page.path = 'relevance/list';
  return post(BASE_URL + buildPagePath(page), param);
}

export function testPlanLoadRelevance(param) {
  return post(BASE_URL + 'relevance', param);
}
//
// export function testPlanLoadEnv(param) {
//   return post(BASE_URL + 'env', param);
// }
//
// export function testPlanLoadCaseBatchUpdateEnv(param) {
//   return post(BASE_URL + 'batch/update/env', param);
// }

export function testPlanLoadCaseUpdate(param) {
  return post(BASE_URL + 'update', param);
}

export function testPlanLoadCaseReportExist(param) {
  return post(BASE_URL + 'report/exist', param);
}

export function testPlanLoadCaseRun(param) {
  return post(BASE_URL + 'run', param);
}
export function testPlanLoadCaseRunBatch(param) {
  return post(BASE_URL + 'run/batch', param);
}

export function testPlanLoadCaseBatchDelete(param) {
  return post(BASE_URL + 'batch/delete', param);
}

export function testPlanLoadCaseSelectAllTableRows(param) {
  return post(BASE_URL + 'selectAllTableRows', param);
}

export function testPlanLoadCaseDelete(id) {
  return get(BASE_URL + `delete/${id}`);
}

export function testPlanLoadCaseGet(id) {
  return get(BASE_URL + `get/${id}`);
}

export function testPlanLoadCaseGetLoadConfig(loadCaseId) {
  return get(BASE_URL + `get-load-config/${loadCaseId}`);
}

export function testPlanLoadCaseGetAdvancedConfig(loadCaseId) {
  return get(BASE_URL + `get-advanced-config/${loadCaseId}`);
}
