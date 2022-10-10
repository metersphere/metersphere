import {get, post} from "metersphere-frontend/src/plugins/request"

const BASE_URL = "/ui/automation/";

export function uiAutomationReduction(param) {
  return post(BASE_URL + 'reduction', param);
}

export function uiAutomationVerifySeleniumServer() {
  return get(BASE_URL + 'verify/seleniumServer/sys');
}

export function uiAutomationRelevance(param) {
  return post(BASE_URL + 'relevance', param);
}
