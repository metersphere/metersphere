import {post} from "metersphere-frontend/src/plugins/request";

const BASE_URL = "/case/review/node/";

export function getTestCaseReviewNodes(projectId, param) {
  return post(BASE_URL + "list/" + projectId, param);
}

export function testCaseReviewNodeAdd(param) {
  return post(BASE_URL + 'add', param);
}

export function testCaseReviewNodeEdit(param) {
  return post(BASE_URL + 'edit', param);
}

export function testCaseReviewNodeDelete(param) {
  return post(BASE_URL + 'delete', param);
}

export function testCaseReviewNodeDrag(param) {
  return post(BASE_URL + 'drag', param);
}

export function testCaseReviewNodePos(param) {
  return post(BASE_URL + 'pos', param);
}
