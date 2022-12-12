import {post, get} from "metersphere-frontend/src/plugins/request";

const BASE_URL = '/test/review/case/';

export function getReviewerStatusComment(id) {
  return get(BASE_URL + `reviewer/status/${id}`);
}
