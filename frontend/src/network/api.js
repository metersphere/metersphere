import {post} from "@/common/js/ajax";
import {success} from "@/common/js/message";
import {baseGet} from "@/network/base-network";

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


