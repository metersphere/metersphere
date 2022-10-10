import {post} from "metersphere-frontend/src/plugins/request"

export function getAnalysisReport(param) {
  return post("/report/test/analysis/getReport", param)
}

export function getCountReport(param) {
  return post("/report/test/case/count/getReport", param)
}

export function initCountData(param) {
  return post("/report/test/case/count/initDatas", param)
}

