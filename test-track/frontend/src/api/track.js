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

export function getTrackRunningTask(selectProjectId, param) {
  return post("/task/center/runningTask/" + selectProjectId, param);
}

export function getTrackBugCount(selectProjectId) {
  return get("/track/bug/count/" + selectProjectId);
}

