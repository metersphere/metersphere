import {get, post} from "metersphere-frontend/src/plugins/request"
const BASE_URL = "/test/case/comment/";

export function testCaseCommentList(id) {
  return get(BASE_URL + `list/${id}`);
}

export function testCaseCommentListByType(id, type) {
  return get(BASE_URL + `list/${id}/${type}`);
}

export function testCaseCommentListByTypeAndBelongId(id, type, belongId) {
  return get(BASE_URL + `list/${id}/${type}/${belongId}`);
}

export function testCaseCommentAdd(param) {
  return post(BASE_URL + 'save', param);
}

