import { get, post } from "metersphere-frontend/src/plugins/request";
import { getCurrentWorkspaceId } from "metersphere-frontend/src/utils/token";

export function getProject(projectId) {
  let url = "/project/get/" + projectId;
  return get(url);
}

export function getOwnerProjectIds() {
  let url = "/api/project/get/owner/ids";
  return get(url);
}

export function getMaintainer() {
  getAll;
  let url = "/user/project/member/list";
  return get(url);
}

export function getProjectConfig(projectId, type) {
  let url = "/project_application/get/config/" + projectId + type;
  return get(url);
}

export function getProjectApplicationConfig(projectId, type) {
  let url = "/project_application/get/" + projectId + type;
  return get(url);
}

export function getProjectMemberOption() {
  return get("/user/project/member/option");
}

export function getProjectMemberById(projectId) {
  return get("/user/project/member/" + projectId);
}

export function getOwnerProjects() {
  return get("/api/project/get/owner/details");
}

export function getAll() {
  return get("/project/listAll/" + getCurrentWorkspaceId());
}

export function getUserWorkspace() {
  return get("/workspace/list/userworkspace/");
}

export function projectRelated(params) {
  return post("/project/list/related", params);
}

export function apiProjectRelated(params) {
  return post("/api/project/list/related", params);
}
