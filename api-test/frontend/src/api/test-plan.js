import {get, post} from "metersphere-frontend/src/plugins/request"
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export function getPlanStageOption() {
  let projectID = getCurrentProjectID();
  return get('/test/plan/get/stage/option/' + projectID);
}

export function planPage(page, pageSize, params) {
  return post('/test/plan/list/all/', params);
}
