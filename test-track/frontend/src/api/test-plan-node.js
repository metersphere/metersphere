import {post} from "metersphere-frontend/src/plugins/request";

const BASE_URL = "/plan/node/";

export function getTestPlanNodes(projectId, param) {
  return post(BASE_URL + "list/" + projectId, param);
}

export function testPlanNodeAdd(param) {
  return post(BASE_URL + 'add', param);
}

export function testPlanNodeEdit(param) {
  return post(BASE_URL + 'edit', param);
}

export function testPlanNodeDelete(param) {
  return post(BASE_URL + 'delete', param);
}

export function testPlanNodeDrag(param) {
  return post(BASE_URL + 'drag', param);
}

export function testPlanNodePos(param) {
  return post(BASE_URL + 'pos', param);
}
