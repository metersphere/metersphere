import {get, post} from "metersphere-frontend/src/plugins/request"

const BASE_URL = '/environment/';

export function getEnvironmentByProjectId(projectId) {
  return get(BASE_URL + `list/${projectId}`);
}
