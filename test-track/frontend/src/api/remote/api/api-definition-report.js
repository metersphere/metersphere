import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/api/definition/report/';

export function apiDefinitionPlanReportGetByCaseId(caseId) {
  return get(BASE_URL + `plan/getReport/${caseId}/API_PLAN`);
}

export function getApiReportDetail(id) {
  let url = '/api/definition/report/get/' + id;
  return get(url);
}
