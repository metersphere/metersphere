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

export function getApiReport(reportId, callback) {
  return reportId ? baseGet('/api/definition/report/getReport/' + reportId, callback) : {};
}


