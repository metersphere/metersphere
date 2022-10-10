import {post, get} from "@/business/utils/sdk-utils";
import {buildListPagePath, buildPagePath} from "@/api/base-network";

const BASE_URL = '/test/plan/api/case/';

export function testPlanApiCaseList(page, param) {
  return post(BASE_URL + buildListPagePath(page), param);
}

export function testPlanApiCaseRelevanceList(page, param) {
  page.path = 'relevance/list';
  return post(BASE_URL + buildPagePath(page), param);
}
//
// export function scenarioRelevance(param) {
//   return post(BASE_URL + 'relevance', param);
// }

export function testPlanApiCaseBatchDelete(param) {
  return post(BASE_URL + 'batch/delete', param);
}

export function testPlanApiCaseSelectAllTableRows(param) {
  return post(BASE_URL + 'select-rows', param);
}

export function testPlanApiCaseRun(param) {
  return post(BASE_URL + 'run', param);
}

export function testPlanApiCaseBatchUpdateEnv(param) {
  return post(BASE_URL + 'batch/update/env', param);
}
//
// export function testPlanTestCaseBatchEdit(param) {
//   return post(BASE_URL + 'batch/edit', param);
// }
//
export function testPlanApiCaseDelete(id) {
  return get(BASE_URL + `delete/${id}`);
}

// export function testPlanTestCaseEdit(param) {
//   return post(BASE_URL + 'edit', param);
// }

// export function testPlanTestCaseGet(id) {
//   return get(BASE_URL + `get/${id}`);
// }

export function apiCaseModulePlanList(planId, protocol) {
  return get(BASE_URL + `list/module/${planId}/${protocol}`);
}

export function getApiCaseEnv(param) {
  return post(BASE_URL + 'env', param);
}
