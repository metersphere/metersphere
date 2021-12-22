import {baseGet} from "@/network/base-network";
import {getCurrentProjectID} from "@/common/js/utils";

export function getProject(projectId, callback) {
  return projectId ? baseGet('/project/get/' + projectId, callback) : {};
}

export function getCurrentProject(callback) {
  return getProject(getCurrentProjectID(), callback);
}
