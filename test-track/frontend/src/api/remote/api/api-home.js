import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/home/';

export function homeTestPlanFailureCaseGet(projectId, selectFunctionCase, limitNumber, currentPage, pageSize) {
  return get(BASE_URL + `failure/case/about/plan/${projectId}/${selectFunctionCase}/${limitNumber}/${currentPage}/${pageSize}`);
}
