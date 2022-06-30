import {baseGet} from "@/network/base-network";
import {getCurrentProjectID, hasLicense} from "@/common/js/utils";

export function getProject(projectId, callback) {
  return projectId ? baseGet('/project/get/' + projectId, callback) : {};
}

export function getCurrentProject(callback) {
  return getProject(getCurrentProjectID(), callback);
}

export function getVersionFilters(projectId, callback) {
  return hasLicense() && projectId ?
    baseGet('/project/version/get-project-versions/' + projectId, data => {
      let versionFilters = data.map(u => {
        return {text: u.name, value: u.id};
      });
      if (callback) {
        callback(versionFilters);
      }
    }) : {};
}
