import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/test/plan/case/';

export function testPlanCaseMinderEdit(param) {
  return post(BASE_URL + 'minder/edit', param);
}
