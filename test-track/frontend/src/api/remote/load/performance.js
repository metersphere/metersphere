import {post, get} from "@/business/utils/sdk-utils";
const BASE_URL = '/performance/';

export function loadTestListBatch(param) {
  return post(BASE_URL + 'list/batch', param);
}
export function loadTestGetJmxContent(testId) {
  return get(BASE_URL + `get-jmx-content/${testId}`);
}
