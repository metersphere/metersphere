import {get, post} from "metersphere-frontend/src/plugins/request"
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export function getProject(id) {
  return get(`/project/get/${id}`)
}

export function getProjectMemberSize(id) {
  return get(`/project/member/size/${id}`)
}

export function getCurrentProject(callback) {
  return getProject(getCurrentProjectID(), callback);
}


export function getOwnerProjectIds() {
  let url = '/api/project/get/owner/ids';
  return get(url);
}

export function getMaintainer() {
  let url = '/user/project/member/list';
  return get(url);
}

// export function getProjectVersions(projectId) {
//   return get('/project/version/get-project-versions/' + projectId);
// }

export function versionEnableByProjectId(projectId) {
  return get('/project/version/enable/' + projectId);
}

export function getProjectConfig(projectId, type) {
  let url = '/project_application/get/config/' + projectId + type;
  return get(url);
}

export function getNodeOperationInfo(request) {
  return post(`/prometheus/query/node-operation-info`, request)
}

export function apiTestReRun(condition) {
  return post('/api/test/exec/rerun', condition);
}

export function projectRelated(params) {
  return post('/project/list/related', params);
}
