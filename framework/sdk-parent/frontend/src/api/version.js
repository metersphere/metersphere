import {get} from "../plugins/request"

export function getProjectVersions(projectId) {
  return get(`/project/version/get-project-versions/${projectId}`);
}


export function getProjectMembers() {
  return get('/user/project/member/list');
}

export function isProjectVersionEnable(projectId) {
  return get(`/project/version/enable/${projectId}`)
}
