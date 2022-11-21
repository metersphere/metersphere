import { post, get } from 'metersphere-frontend/src/plugins/request';

export function getReportPage(currentPage, pageSize, condition) {
  let url = '/api/scenario/report/list/' + currentPage + '/' + pageSize;
  return post(url, condition);
}

export function getReportPageDetail(currentPage, pageSize, condition) {
  let url = '/api/scenario/report/list/' + currentPage + '/' + pageSize;
  return post(url, condition);
}

export function delReport(id) {
  let url = '/api/scenario/report/delete';
  return post(url, { id: id });
}

export function delBatchReport(condition) {
  let url = '/api/scenario/report/batch/delete';
  return post(url, condition);
}

export function reportReName(condition) {
  let url = '/api/scenario/report/rename';
  return post(url, condition);
}

export function getScenarioReport(reportId) {
  return get('/api/scenario/report/get/' + reportId);
}

export function getScenarioReportDetail(reportId) {
  return get('/api/scenario/report/get/detail/' + reportId);
}

export function getShareScenarioReport(shareId, reportId) {
  return get('/api/scenario/report/get/' + shareId + '/' + reportId);
}

export function getScenarioReportStepDetail(stepId) {
  return get('/api/scenario/report/get/step/detail/' + stepId);
}

export function updateReport(data) {
  return post('/api/scenario/report/update', data);
}
