import {get, post} from "metersphere-frontend/src/plugins/request"
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export function listAllProject() {
  return get("/project/listAll")
}

export function getProject(id) {
  return get(`/project/get/${id}`)
}

export function versionEnableByProjectId(projectId) {
  return get('/project/version/enable/' + projectId);
}

export function getUserProjectList(data) {
  return post("/project/list/related", data)
}

export function getOwnerProjectIds() {
  let url = '/api/project/get/owner/ids';
  return get(url);
}

export function getOwnerProjects() {
  return get('/api/project/get/owner/details');
}

export function getCurrentProject(callback) {
  return getProject(getCurrentProjectID(), callback);
}

export function getProjectApplication(projectId){
  return get('/project_application/get/config/' + projectId + '/OPEN_UPDATE_RULE')
}

export function getProjectConfig(projectId) {
  let url = '/project_application/get/config/' + projectId;
  return get(url);
}
