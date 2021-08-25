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
