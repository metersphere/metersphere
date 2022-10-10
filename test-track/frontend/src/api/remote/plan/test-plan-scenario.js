import {post, get} from "@/business/utils/sdk-utils";
import {buildListPagePath, buildPagePath} from "@/api/base-network";

const BASE_URL = '/test/plan/scenario/case/';

export function testPlanScenarioList(page, param) {
  return post(BASE_URL + buildListPagePath(page), param);
}

export function scenarioRelevanceList(page, param) {
  page.path = 'relevance/list';
  return post(BASE_URL + buildPagePath(page), param);
}

export function scenarioRelevance(param) {
  return post(BASE_URL + 'relevance', param);
}

export function testPlanScenarioEnv(param) {
  return post(BASE_URL + 'env', param);
}

export function testPlanScenarioCaseBatchUpdateEnv(param) {
  return post(BASE_URL + 'batch/update/env', param);
}

export function testPlanScenarioCaseRun(param) {
  return post(BASE_URL + 'run', param);
}

export function testPlanScenarioCaseBatchDelete(param) {
  return post(BASE_URL + 'batch/delete', param);
}

export function testPlanScenarioCaseSelectAllTableRows(param) {
  return post(BASE_URL + 'select-rows', param);
}

export function testPlanScenarioCaseDelete(id) {
  return get(BASE_URL + `delete/${id}`);
}

export function apiAutomationModulePlanList(planId) {
  return get(BASE_URL + `list/module/${planId}`);
}
