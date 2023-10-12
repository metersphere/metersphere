import {get, post} from "metersphere-frontend/src/plugins/request";

export function getPerformanceReport(reportId) {
  return get('/performance/report/' + reportId);
}

export function getSharePerformanceReport(shareId, reportId) {
  return get('/share/performance/report/' + shareId + '/' + reportId);
}

export function getPerformanceReportTime(reportId) {
  return get('/performance/report/content/report_time/' + reportId);
}

export function getSharePerformanceReportTime(shareId, reportId) {
  return get('/share/performance/report/content/report_time/' + shareId + '/' + reportId);
}

export function getPerformanceLoadConfig(reportId) {
  return get('/performance/report/get-load-config/' + reportId);
}

export function getSharePerformanceLoadConfig(shareId, reportId) {
  return get('/share/performance/report/get-load-config/' + shareId + '/' + reportId);
}

export function getPerformanceJmxContent(reportId) {
  return get('/performance/report/get-jmx-content/' + reportId);
}

export function getSharePerformanceJmxContent(shareId, reportId) {
  return get('/share/performance/report/get-jmx-content/' + shareId + '/' + reportId);
}

export function getOldPerformanceJmxContent(testId) {
  return get('/performance/get-jmx-content/' + testId);
}

export function getShareOldPerformanceJmxContent(testId) {
  return get('/performance/get-jmx-content/' + testId);
}

export function getPerformanceReportOverview(reportId) {
  return get('/performance/report/content/testoverview/' + reportId);
}

export function getSharePerformanceReportOverview(shareId, reportId) {
  return get('/share/performance/report/content/testoverview/' + shareId + '/' + reportId);
}

export function getPerformanceReportLoadChart(reportId) {
  return get('/performance/report/content/load_chart/' + reportId);
}

export function getSharePerformanceReportLoadChart(shareId, reportId) {
  return get('/share/performance/report/content/load_chart/' + shareId + '/' + reportId);
}

export function getPerformanceReportResChart(reportId) {
  return get('/performance/report/content/res_chart/' + reportId);
}

export function getSharePerformanceReportResChart(shareId, reportId) {
  return get('/share/performance/report/content/res_chart/' + shareId + '/' + reportId);
}

export function getPerformanceReportErrorChart(reportId) {
  return get('/performance/report/content/error_chart/' + reportId);
}

export function getSharePerformanceReportErrorChart(shareId, reportId) {
  return get('/share/performance/report/content/error_chart/' + shareId + '/' + reportId);
}

export function getPerformanceReportResponseCodeChart(reportId) {
  return get('/performance/report/content/response_code_chart/' + reportId);
}

export function getSharePerformanceReportResponseCodeChart(shareId, reportId) {
  return get('/share/performance/report/content/response_code_chart/' + shareId + '/' + reportId);
}

export function getPerformanceReportDetailContent(reportKey, reportId) {
  return get('/performance/report/content/' + reportKey + '/' + reportId)
}

export function getSharePerformanceReportDetailContent(shareId, reportKey, reportId) {
  return get('/share/performance/report/content/' + shareId + '/' + reportKey + '/' + reportId);
}

export function getPerformanceReportContent(reportId) {
  return get('/performance/report/content/' + reportId);
}

export function getSharePerformanceReportContent(shareId, reportId) {
  return get('/share/performance/report/content/' + shareId + '/' + reportId);
}

export function getPerformanceReportErrorsContent(reportId) {
  return get('/performance/report/content/errors/' + reportId);
}

export function getSharePerformanceReportErrorsContent(shareId, reportId) {
  return get('/share/performance/report/content/errors/' + shareId + '/' + reportId);
}

export function getPerformanceReportErrorsTop5(reportId) {
  return get('/performance/report/content/errors_top5/' + reportId);
}

export function getPerformanceReportErrorSamples(reportId) {
  return get('/performance/report/content/errors_samples/' + reportId);
}

export function getSharePerformanceReportErrorSamples(shareId, reportId) {
  return get('/share/performance/report/content/errors_samples/'  + shareId + '/' + reportId);
}
export function getSharePerformanceReportErrorsTop5(shareId, reportId) {
  return get('/share/performance/report/content/errors_top5/' + shareId + '/' + reportId);
}

export function getPerformanceReportLogResource(reportId) {
  return get('/performance/report/log/resource/' + reportId);
}

export function getSharePerformanceReportLogResource(shareId, reportId) {
  return get('/share/performance/report/log/resource/' + shareId + '/' + reportId);
}

export function getPerformanceReportLogResourceDetail(reportId, resourceId, pageInfo) {
  return get('/performance/report/log/' + reportId + '/' + resourceId + "/" + pageInfo);
}

export function getSharePerformanceReportLogResourceDetail(shareId, reportId, resourceId, pageInfo) {
  return get('/share/performance/report/log/' + shareId + '/' + reportId + '/' + resourceId + "/" + pageInfo);
}

export function getPerformanceMetricQueryResource(resourceId) {
  return get('/metric/query/resource/' + resourceId)
}

export function getSharePerformanceMetricQueryResource(shareId, resourceId) {
  return get('/share/metric/query/resource/' + shareId + '/' + resourceId)
}

export function getPerformanceMetricQuery(resourceId) {
  return get('/metric/query/' + resourceId);
}

export function getSharePerformanceMetricQuery(shareId, resourceId) {
  return get('/share/metric/query/' + shareId + '/' + resourceId)
}

export function editLoadTestCaseOrder(request) {
  return post('/performance/edit/order', request);
}
