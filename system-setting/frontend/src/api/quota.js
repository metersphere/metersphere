import { get, post } from "metersphere-frontend/src/plugins/request";

export function getWorkspaceValidResourcePool(workspaceId) {
  return get(`/testresourcepool/list/quota/ws/valid/${workspaceId}`);
}

export function getWorkspaceModules(type, id) {
  return get(`/quota/list/modules/${type}/${id}`);
}

export function getProjectDefaultQuota(workspaceId) {
  return get(`/quota/default/project/${workspaceId}`);
}

export function saveProjectDefaultQuota(quota) {
  return post("/quota/save/default/project", quota);
}

export function getAllValidResourcePool() {
  return get("/testresourcepool/list/all/valid");
}

export function getWorkspaceDefaultQuota() {
  return get("/quota/default/workspace");
}

export function saveWorkspaceDefaultQuota(quota) {
  return post("/quota/save/default/workspace", quota);
}

/**
 * query quota list page
 * @param queryType QUOTA_TYPE => workspace || project
 * @param goPage
 * @param pageSize
 * @param params
 * @returns {Promise | Promise<unknown>}
 */
export function getQuotaPages(queryType, goPage, pageSize, params) {
  return post(`/quota/list/${queryType}/${goPage}/${pageSize}`, params);
}

export function deleteQuota(quota) {
  return post("/quota/delete", quota);
}

export function saveQuota(quota) {
  return post("/quota/save", quota);
}

export function getProjectModules(type, id) {
  return get(`/quota/list/modules/${type}/${id}`);
}
