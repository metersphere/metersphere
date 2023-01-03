import {post, get} from "metersphere-frontend/src/plugins/request";
import {buildPagePath} from "@/api/base-network";

const BASE_URL = '/test/case/review/';

export function getRelateTestCaseReview(currentPage, pageSize, param) {
  return post(BASE_URL + 'list/all/relate/' + currentPage + "/" + pageSize, param);
}

export function testReviewApiCaseList(page, param) {
  page.path = 'api/case/list';
  return post(BASE_URL + buildPagePath(page), param);
}

export function testReviewListAll() {
  return post(BASE_URL + 'list/all');
}

export function testReviewList(currentPage, pageSize, param) {
  return post(BASE_URL + 'list/' + currentPage + "/" + pageSize, param);
}

export function getTestCaseReviewProject(param) {
  return post(BASE_URL + 'project', param);
}

export function getTestCaseReviewReviewer(param) {
  return post(BASE_URL + 'reviewer', param);
}

export function getTestCaseReviewFollow(param) {
  return post(BASE_URL + 'follow', param);
}

export function deleteTestCaseReview(reviewId) {
  return get(BASE_URL + 'delete/' + reviewId);
}

export function editTestCaseReviewFollows(param) {
  return post(BASE_URL + 'edit/follows', param);
}

export function saveOrUpdateTestCaseReview(operationType, param) {
  return post(BASE_URL + operationType, param);
}

export function getTestCaseReviewRelevance(param) {
  return post(BASE_URL + 'relevance', param);
}

export function getTesReviewById(reviewId) {
  return get(BASE_URL + "get/" + reviewId);
}

export function editTestCaseReviewStatus(reviewId) {
  return post(BASE_URL + "edit/status/" + reviewId);
}

export function getTestReviewTestCase(caseId) {
  return get("/test/review/case/get/" + caseId);
}

export function editTestReviewTestCase(param) {
  return post("/test/review/case/edit", param);
}

export function batchDeleteTestReviewCase(param) {
  return post("/test/review/case/batch/delete", param);
}

export function deleteTestReviewCase(param) {
  return post("/test/review/case/delete", param);
}

export function batchEditTestReviewCaseStatus(param) {
  return post("/test/review/case/batch/edit/status", param);
}

export function batchEditTestReviewCaseReviewer(param) {
  return post("/test/review/case/batch/edit/reviewer", param);
}

export function getTestCaseReviewsCasePage(currentPage, pageSize, param) {
  return post("/test/case/reviews/case/" + currentPage + "/" + pageSize, param);
}

export function getRelateTest(type, testCaseId) {
  return get("/" + type + "/get/" + testCaseId);
}
