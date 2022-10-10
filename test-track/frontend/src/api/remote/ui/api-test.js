import {get, post} from "metersphere-frontend/src/plugins/request"

const BASE_URL = "/api/test/";

export function apiTestExecRerun(param) {
  return post(BASE_URL + 'exec/rerun', param);
}
