import {get, post} from "@/business/utils/sdk-utils";

const BASE_URL = '/environment/';

export function getEnvironmentByProjectId(projectId) {
  return get(BASE_URL + `list/${projectId}`);
}
