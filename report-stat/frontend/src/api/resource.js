import {get} from "metersphere-frontend/src/plugins/request"

let basePath = "/report/resource";

export function selectTestCaseNodeList(param) {
  return get(basePath + "/test/case/node/" + param)
}

export function selectUserProjectMember() {
  return get(basePath + "/project/member/list")
}
