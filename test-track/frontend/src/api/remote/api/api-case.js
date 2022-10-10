import {post, get} from "@/business/utils/sdk-utils";
import {buildPagePath} from "@/api/base-network";

const BASE_URL = '/api/testcase/';

export function apiTestCaseListBlobs(param) {
  return post(BASE_URL + 'list-blobs', param);
}

export function apiTestCaseReduction(param) {
  return post(BASE_URL + 'reduction', param);
}

export function apiTestCaseRelevanceList(page, param) {
  page.path = 'relevance';
  return post(BASE_URL + buildPagePath(page), param);
}

export function apiTestCaseRelevance(param) {
  return post(BASE_URL + 'relevance', param);
}

export function apiTestCaseGet(id) {
  return get(BASE_URL + `get/${id}`);
}
