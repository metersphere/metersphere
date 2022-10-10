import {post, get} from "@/business/utils/sdk-utils";
import {buildListPagePath} from "@/api/base-network";

const BASE_URL = '/test/plan/report/';

export function testPlanReportList(page, param) {
  return post(BASE_URL + buildListPagePath(page), param);
}

export function testPlanReportDelete(param) {
  return post(BASE_URL + 'delete', param);
}

export function testPlanReportBatchDelete(param) {
  return post(BASE_URL + 'deleteBatchByParams', param);
}

export function testPlanReportReName(param) {
  return post(BASE_URL + 'reName', param);
}

export function testPlanReportGetDb(id) {
  return get(BASE_URL + `db/${id}`);
}
