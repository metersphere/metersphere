import {get, post} from "metersphere-frontend/src/plugins/request"

const BASE_URL = "/api/automation/";

export function getApiScenarioEnvByProjectId(id) {
  return get(BASE_URL + `env-project-ids/${id}`);
}

export function apiAutomationReduction(param) {
  return post(BASE_URL + 'reduction', param);
}
