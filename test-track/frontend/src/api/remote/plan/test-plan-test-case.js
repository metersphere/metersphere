import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/test/plan/case/';

export function testPlanTestCaseBatchDelete(param) {
  return post(BASE_URL + 'batch/delete', param);
}

export function testPlanTestCaseBatchEdit(param) {
  return post(BASE_URL + 'batch/edit', param);
}

export function testPlanTestCaseDelete(id) {
  return post(BASE_URL + `delete/${id}`, {});
}

export function testPlanTestCaseEdit(param) {
  return post(BASE_URL + 'edit', param);
}

export function testPlanTestCaseGet(id) {
  return get(BASE_URL + `get/${id}`);
}
