import {get} from "../plugins/request"
import {hasLicense} from "../utils/permission";

export function getProjectVersions(projectId) {
  return get(`/project/version/get-project-versions/${projectId}`);
}

export function getProjectMembers() {
  return get('/user/project/member/list');
}

export function isProjectVersionEnable(projectId) {
  return get(`/project/version/enable/${projectId}`)
}

export function getVersionFilters(projectId) {
  return hasLicense() && projectId ? new Promise((resolve) => {
    getProjectVersions(projectId)
      .then((r) => {
        let versionFilters = r.data.map(u => {
          return {text: u.name, value: u.id};
        });
        resolve({data: versionFilters});
      });
  }) : new Promise(resolve => resolve({}));
}
