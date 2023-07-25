import {get, post, request, socket} from "metersphere-frontend/src/plugins/request";

export function getReportTime(id) {
  return get(`/performance/report/content/report_time/${id}`)
}

export function getOverview(id) {
  return get(`/performance/report/content/testoverview/${id}`)
}

export function searchReports(goPage, pageSize, param) {
  return post(`/performance/report/list/all/${goPage}/${pageSize}`, param)
}

export function renameReport(data) {
  return post(`/performance/report/rename`, data)
}

export function deleteReport(id) {
  return post(`/performance/report/delete/${id}`, {});
}

export function deleteReportBatch(param) {
  return post(`/performance/report/batch/delete`, param);
}

export function getTestProInfo(id) {
  return get(`/performance/report/test/pro/info/${id}`);
}

export function stopTest(id, force) {
  return get(`/performance/stop/${id}/${force}`)
}

export function getProjectApplication(projectId) {
  return get(`/project_application/get/${projectId}/PERFORMANCE_SHARE_REPORT_TIME`)
}

export function getReport(id) {
  return get(`/performance/report/${id}`)
}

export function downloadLogFile(id, resourceId, isShare) {
  let url = '/performance/report/log/download/' + id + '/' + resourceId;
  if (isShare) {
    url = '/share/performance/report/log/download/' + id + '/' + resourceId;
  }
  let config = {
    url: url,
    method: 'get',
    responseType: 'blob'
  };
  return request(config)
}

export function downloadZip(report) {
  let testId = report.testId;
  let reportId = report.id;
  let resourceIndex = 0;
  let ratio = "-1";
  let config = {
    url: `/jmeter/download?testId=${testId}&ratio=${ratio}&reportId=${reportId}&resourceIndex=${resourceIndex}`,
    method: 'get',
    responseType: 'blob'
  };

  return request(config);
}

export function initReportSocket(reportId) {
  return socket(`/websocket/performance/report/${reportId}`);
}

