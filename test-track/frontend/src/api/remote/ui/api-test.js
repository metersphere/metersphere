import {post} from "metersphere-frontend/src/plugins/request"

const BASE_URL = "/test/plan/";

export function apiTestExecRerun(param) {
  return post(BASE_URL + 'rerun', param);
}
