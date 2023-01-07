import {post, get} from "@/business/utils/sdk-utils";
import {buildListPagePath, buildPagePath} from "@/api/base-network";

const BASE_URL = '/test/plan/uiScenario/case/';

export function testPlanUiScenarioList(page, param) {
  return post(BASE_URL + buildListPagePath(page), param);
}

export function testPlanUiScenarioRelevanceList(page, param) {
  page.path = 'relevance/list';
  return post(BASE_URL + buildPagePath(page), param);
}

export function testPlanUiScenarioRelevanceListIds(param) {
  return post(BASE_URL + 'relevance/list/ids', param);
}

export function testPlanUiScenarioEnv(param) {
  return post(BASE_URL + 'env', param);
}

export function testPlanUiScenarioCaseBatchUpdateEnv(param) {
  return post(BASE_URL + 'batch/update/env', param);
}

export function testPlanUiScenarioCaseRun(param) {
  return post(BASE_URL + 'run', param);
}

export function testPlanUiScenarioCaseBatchDelete(param) {
  return post(BASE_URL + 'batch/delete', param);
}

export function testPlanUiScenarioCaseSelectAllTableRows(param) {
  return post(BASE_URL + 'selectAllTableRows', param);
}

export function testPlanUiScenarioCaseDelete(id) {
  return get(BASE_URL + `delete/${id}`, {});
}

export function uiScenarioModulePlanList(planId) {
  return get(BASE_URL + `list/module/${planId}`);
}

export function testPlanUiScenarioCaseEnv(params) {
  return post(BASE_URL + 'get/env', params);
}
