import {get, post} from "metersphere-frontend/src/plugins/request"

let baseUrl = '/ui/scenario/module/';
let trashUrl = '/ui/scenario/module/trash/';

export function getScenarioModules(projectId, isTrashData, type) {
  let url = isTrashData ? trashUrl : baseUrl;
  url = url + 'list/' + projectId
  if(type){
    url = `${url}?type=${type}`;
  }
  return get(url);
}

export function addScenarioModule(param) {
  return post(baseUrl + 'add', param);
}

export function editScenarioModule(param) {
  return post(baseUrl + 'edit', param);
}

export function dragScenarioModule(param) {
  return post(baseUrl + 'drag', param);
}

export function posScenarioModule(param, callback) {
  return post(baseUrl + 'pos', param, callback);
}

export function deleteScenarioModule(nodeIds, callback) {
  return post(baseUrl + 'delete', nodeIds, callback);
}

export function getUiAutomationList(currentPage, pageSize, param) {
  return post("/ui/automation/list/" + currentPage + "/" + pageSize, param);
}

