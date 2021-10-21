import {post} from "@/common/js/ajax";
import {success} from "@/common/js/message";
import {baseGet, basePost} from "@/network/base-network";

export function apiCaseBatchRun(condition) {
  return post('/api/testcase/batch/run', condition, () => {
    success("执行成功，请稍后刷新查看");
  });
}

export function getScenarioReport(reportId, callback) {
  return reportId ? baseGet('/api/scenario/report/get/' + reportId, callback) : {};
}

export function getApiReport(testId, callback) {
  return testId ? baseGet('/api/definition/report/getReport/' + testId, callback) : {};
}

export function getShareApiReport(shareId, testId, callback) {
  return testId ? baseGet('/share/api/definition/report/getReport/' + shareId + '/' + testId, callback) : {};
}

export function getShareScenarioReport(shareId, reportId, callback) {
  return reportId ? baseGet('/share/api/scenario/report/get/' + shareId + '/' + reportId, callback) : {};
}

export function editApiDefinitionOrder(request, callback) {
  return basePost('/api/definition/edit/order', request, callback);
}

export function editApiTestCaseOrder(request, callback) {
  return basePost('/api/testcase/edit/order', request, callback);
}

export function getRelationshipApi(id, relationshipType, callback) {
  return baseGet('/api/definition/relationship/' + id + '/' + relationshipType, callback);
}

export function getRelationshipCountApi(id, callback) {
  return baseGet('/api/definition/relationship/count/' + id + '/', callback);
}
