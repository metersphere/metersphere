import {post} from "metersphere-frontend/src/plugins/request"

let basePath = '/history/report';

export function selectHistoryReportByParams(param) {
  return post(basePath + '/selectByParams', param)
}

export function selectHistoryReportById(param) {
  return post(basePath + '/selectById', param)
}

export function createHistoryReport(param) {
  return post(basePath + '/create', param)
}

export function updateHistoryReport(param) {
  return post(basePath + '/update', param)
}

export function updateHistoryReportByRequest(param) {
  return post(basePath + '/updateByRequest', param)
}


export function deleteHistoryReportByParams(param) {
  return post(basePath + '/deleteByParam', param)
}


