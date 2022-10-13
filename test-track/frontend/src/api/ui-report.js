import {get, post} from "metersphere-frontend/src/plugins/request"

export function getScenarioReport(reportId) {
  return reportId ? get('/ui/scenario/report/get/' + reportId) : {};
}

export function getScenarioReportAll(reportId) {
  return reportId ? get('/ui/scenario/report/getAll/' + reportId) : {};
}

export function getApiReport(testId) {
  return testId ? get('/api/definition/report/getReport/' + testId) : {};
}

export function getShareApiReport(shareId, testId) {
  return testId ? get('/share/api/definition/report/getReport/' + shareId + '/' + testId) : {};
}

export function getShareScenarioReport(shareId, reportId) {
  return reportId ? get('/share/ui/scenario/report/get/' + shareId + '/' + reportId) : {};
}
