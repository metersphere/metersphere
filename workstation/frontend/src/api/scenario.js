import { get, post } from "metersphere-frontend/src/plugins/request";

export function getScenarioById(scenarioId) {
  return get("/api/automation/get/" + scenarioId);
}

export function getScenarioList(currentPage, pageSize, condition) {
  let url = "/api/automation/list/" + currentPage + "/" + pageSize;
  return post(url, condition);
}

export function getScenarioReference(params) {
  return post("/api/automation/getReference", params);
}
