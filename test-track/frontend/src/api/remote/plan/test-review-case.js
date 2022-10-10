import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/test/review/case/';

export function testReviewCaseMinderEdit(reviewId, param) {
  return post(BASE_URL + `minder/edit/${reviewId}`, param);
}
