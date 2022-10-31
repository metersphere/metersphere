import {get} from "metersphere-frontend/src/plugins/request"

export function getProject(id) {
  return get(`/project/get/${id}`)
}

export function getProjectMemberSize(id) {
  return get(`/project/member/size/${id}`)
}
