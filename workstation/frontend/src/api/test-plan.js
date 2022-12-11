import {post,get} from "metersphere-frontend/src/plugins/request";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";


export function getPlanStageOption(projectID) {
  return get('/test/plan/get/stage/option/' + projectID);
}

export function getPrincipalById(id) {
  return get('/test/plan/principal/' + id);
}

export function getDashboardPlanList(goPage, pageSize,param) {
  return post('/test/plan/dashboard/list/' + goPage+ "/" + pageSize,param);
}

export function getPlanList(goPage, pageSize,param) {
  return post('/test/plan/list/' + goPage+ "/" + pageSize,param);
}

export function editPlan(param) {
  return post('/test/plan/edit',param);
}
