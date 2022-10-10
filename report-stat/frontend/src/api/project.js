import {get} from "metersphere-frontend/src/plugins/request"

export function listAllProject(workspaceId) {
  return get(`/project/listAll/${workspaceId}`)
}

export function getProject(id) {
  return get(`/project/get/${id}`)
}

export function getProjectMemberSize(id) {
  return get(`/project/member/size/${id}`)
}
