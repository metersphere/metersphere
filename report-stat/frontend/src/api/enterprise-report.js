import {get, post} from "metersphere-frontend/src/plugins/request"

let basePath = "/enterprise/test/report";

export function getEnterpriseReport(param) {
  return get(basePath + '/get/' + param);
}

export function getEnterpriseReportByParam(currentPage, pageSize, param) {
  return post(basePath + '/list/' + currentPage + "/" + pageSize, param);
}

export function createEnterpriseReport(param) {
  return post(basePath + '/create', param);
}

export function copyEnterpriseReport(param) {
  return post(basePath + '/copy', param);
}

export function updateEnterpriseReport(param) {
  return post(basePath + '/update', param);
}

export function sendEnterpriseReport(param) {
  return post(basePath + '/send', param);
}

export function deleteEnterpriseReport(param) {
  return post(basePath + '/delete', param);
}

export function createScheduleTask(param) {
  return post(basePath + '/schedule/create', param);
}

export function updateScheduleTask(param) {
  return post(basePath + '/schedule/update', param);
}

export function selectScheduleTask(scheduleId, taskType) {
  return get(basePath + '/schedule/findOne/' + scheduleId + '/' + taskType)
}
