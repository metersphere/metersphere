import {post, get} from "metersphere-frontend/src/plugins/request";

const BASE_URL = "/case/node/";

export function testCaseNodeEdit(param) {
  return post(BASE_URL + 'edit', param);
}

export function testCaseNodeAdd(param) {
  return post(BASE_URL + 'add', param);
}

export function testCaseNodeListPlanRelate(param) {
  return post(BASE_URL + 'list/plan/relate', param);
}

export function testCaseNodeDelete(param) {
  return post(BASE_URL + 'delete', param);
}

export function testCaseNodeDrag(param) {
  return post(BASE_URL + 'drag', param);
}

export function testCaseNodePos(param) {
  return post(BASE_URL + 'pos', param);
}

export function testCaseNodeTrashCount(projectId) {
  return get(BASE_URL + `trashCount/${projectId}`);
}

export function testCaseNodePublicCount(workspaceId) {
  return get(BASE_URL + `publicCount/${workspaceId}`);
}

export function testCaseNodeListProject(param) {
  return post(BASE_URL + "list/project", param);
}

export function testCaseNodeListReview(reviewId) {
  return post(BASE_URL + "list/review/" + reviewId);
}

export function testCaseNodeListReviewRelate(param) {
  return post(BASE_URL + "list/review/relate", param);
}

export function getRelationshipNodesByCaseFilter(projectId, param) {
  return post(BASE_URL + 'relationship/list/' + projectId, param);
}
