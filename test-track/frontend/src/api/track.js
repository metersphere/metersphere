import {post, get} from "metersphere-frontend/src/plugins/request";

export function getTrackCount(selectProjectId) {
  return get("/track/count/" + selectProjectId);
}

export function getTrackRelevanceCount(selectProjectId) {
  return get("/track/relevance/count/" + selectProjectId);
}

export function getTrackCaseBar(selectProjectId) {
  return get("/track/case/bar/" + selectProjectId);
}

export function getTrackRunningTask(selectProjectId, currentPage, pageSize, param) {
  return post("/task/center/runningTask/" + selectProjectId + "/" + currentPage + "/" + pageSize, param);
}

export function getTrackBugCount(selectProjectId) {
  return get("/track/bug/count/" + selectProjectId);
}

export function formatNumber(param) {
  let num = (param || 0).toString(), result = '';
  while (num.length > 3) {
    result = ',' + num.slice(-3) + result;
    num = num.slice(0, num.length - 3);
  }
  if (num) {
    result = num + result;
  }
  return result;
}

