import {post,get} from "metersphere-frontend/src/plugins/request";

export function editTestCaseOrder(request, callback) {
  return post('/test/case/edit/order', request, callback);
}

export function getTestCasePages(goPage, pageSize, param) {
  return post(`/test/case/list/${goPage}/${pageSize}`, param);
}

export function getTestCaseListById(id) {
  return get(`test/case/get/${id}`);
}

export function getTestCaseReviewPages(goPage, pageSize, param) {
  return post(`/test/case/review/list/${goPage}/${pageSize}`, param);
}

export function getTestCaseReviewProject(param) {
  return post(`/test/case/review/project`,param);
}

export function getTestCaseReviewer(param) {
  return post(`/test/case/review/reviewer`,param);
}

export function getTestCaseStep(id) {
  return get( `/test/case/get/step/${id}`);
}

export function buildPagePath({pageNum, pageSize, path}) {
  return path + "/" + pageNum + "/" + pageSize;
}

export function testCaseList({pageNum, pageSize}, param) {
  let url = buildPagePath({pageNum, pageSize, path: 'list'});
  return post('/test/case/' + url, param);
}


