import {get, post} from "metersphere-frontend/src/plugins/request"

const BASE_URL = "/ui/automation/";

export function getUiScenarioEnvByProjectId(id) {
  return get(BASE_URL + `env-project-ids/${id}`);
}

export function getUiScenarioIdProject(param) {
  return post(BASE_URL + `id-project`, param);
}

export function uiAutomationReduction(param) {
  return post(BASE_URL + 'reduction', param);
}

export function uiScenarioEnvMap(params) {
  return post(BASE_URL + 'env/map', params);
}
