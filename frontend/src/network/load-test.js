import {baseGet} from "@/network/base-network";

export function getPerformanceReport(reportId, callback) {
  return reportId ? baseGet('/performance/report/' + reportId, callback) : {};
}
export function getSharePerformanceReport(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportTime(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/report_time/' + reportId, callback) : {};
}
export function getSharePerformanceReportTime(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/report_time/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceLoadConfig(reportId, callback) {
  return reportId ? baseGet('/performance/report/get-load-config/' + reportId, callback) : {};
}
export function getSharePerformanceLoadConfig(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/get-load-config/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceJmxContent(reportId, callback) {
  return reportId ? baseGet('/performance/report/get-jmx-content/' + reportId, callback) : {};
}
export function getSharePerformanceJmxContent(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/get-jmx-content/' + shareId + '/' + reportId, callback) : {};
}

export function getOldPerformanceJmxContent(testId, callback) {
  return testId ? baseGet('/performance/get-jmx-content/' + testId, callback) : {};
}
export function getShareOldPerformanceJmxContent(testId, callback) {
  return testId ? baseGet('/performance/get-jmx-content/' + testId, callback) : {};
}

export function getPerformanceReportOverview(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/testoverview/' + reportId, callback) : {};
}
export function getSharePerformanceReportOverview(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/testoverview/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportLoadChart(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/load_chart/' + reportId, callback) : {};
}
export function getSharePerformanceReportLoadChart(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/load_chart/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportResChart(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/res_chart/' + reportId, callback) : {};
}
export function getSharePerformanceReportResChart(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/res_chart/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportErrorChart(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/error_chart/' + reportId, callback) : {};
}
export function getSharePerformanceReportErrorChart(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/error_chart/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportResponseCodeChart(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/response_code_chart/' + reportId, callback) : {};
}
export function getSharePerformanceReportResponseCodeChart(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/response_code_chart/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportDetailContent(reportKey, reportId, callback) {
  return reportId ? baseGet('/performance/report/content/' + reportKey + '/' + reportId, callback) : new Promise(()=>{});
}
export function getSharePerformanceReportDetailContent(shareId, reportKey, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/' + shareId + '/' + reportKey + '/' + reportId, callback) : new Promise(()=>{});
}

export function getPerformanceReportContent(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/' + reportId, callback) : {};
}
export function getSharePerformanceReportContent(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportErrorsContent(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/errors/' + reportId, callback) : {};
}
export function getSharePerformanceReportErrorsContent(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/errors/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportErrorsTop5(reportId, callback) {
  return reportId ? baseGet('/performance/report/content/errors_top5/' + reportId, callback) : {};
}
export function getSharePerformanceReportErrorsTop5(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/content/errors_top5/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportLogResource(reportId, callback) {
  return reportId ? baseGet('/performance/report/log/resource/' + reportId, callback) : {};
}
export function getSharePerformanceReportLogResource(shareId, reportId, callback) {
  return reportId ? baseGet('/share/performance/report/log/resource/' + shareId + '/' + reportId, callback) : {};
}

export function getPerformanceReportLogResourceDetail(reportId, resourceId, pageInfo, callback) {
  return reportId ? baseGet('/performance/report/log/' + reportId + '/' + resourceId + "/" + pageInfo, callback) : {};
}
export function getSharePerformanceReportLogResourceDetail(shareId, reportId, resourceId, pageInfo, callback) {
  return reportId ? baseGet('/performance/report/log/' + shareId + '/' + reportId + '/' + resourceId + "/" + pageInfo, callback) : {};
}

export function getPerformanceMetricQueryResource(resourceId, callback) {
  return resourceId ? baseGet('/metric/query/resource/' + resourceId, callback) : new Promise(()=>{});
}
export function getSharePerformanceMetricQueryResource(shareId, resourceId, callback) {
  return resourceId ? baseGet('/share/metric/query/resource/' + shareId + '/' + resourceId, callback) : new Promise(()=>{});
}

export function getPerformanceMetricQuery(resourceId, callback) {
  return resourceId ? baseGet('/metric/query/' + resourceId, callback) : {};
}
export function getSharePerformanceMetricQuery(shareId, resourceId, callback) {
  return resourceId ? baseGet('/share/metric/query/' + shareId + '/' + resourceId, callback) : new Promise(()=>{});
}
