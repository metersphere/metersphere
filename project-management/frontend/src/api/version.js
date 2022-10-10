import {get, post} from "metersphere-frontend/src/plugins/request"

export function getProjectVersions(projectId) {
  return get(`/project/version/get-project-versions/${projectId}`);
}


export function getProjectMembers() {
  return get('/user/project/member/list');
}

export function isProjectVersionEnable(projectId) {
  return get(`/project/version/enable/${projectId}`)
}

export function changeProjectVersionEnable(versionEnable, projectId) {
  return get(`/project/version/enable/${versionEnable}/${projectId}`)
}

export function listProjectVersions(currentPage, pageSize, condition) {
  return post(`/project/version/list/${currentPage}/${pageSize}`, condition)
}

export function saveProjectVersion(form) {
  let url = "/project/version/add";
  if (form.id) {
    url = '/project/version/edit';
  }
  return post(url, form);
}

export function checkForDelete(id) {
  return get(`/project/version/check-for-delete/${id}`);
}

export function deleteProjectVersion(id) {
  return get(`/project/version/delete/${id}`);
}

export function getProjectVersion(id) {
  return get(`/project/version/get/${id}`);
}

export function changeStatus(row) {
  return get(`/project/version/${row.status}/${row.id}`)
}

export function changeLatest(row) {
  return get(`/project/version/latest/${row.projectId}/${row.id}`)
}
