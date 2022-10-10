import {get, post} from "metersphere-frontend/src/plugins/request"

const BASE_URL = '/graph/';
export function getRelationshipGraph(id, type) {
  return get(BASE_URL + `relationship/graph/${id}/${type}`);
}

export function getGraphByCondition(type, param) {
  return post(BASE_URL + `relationship/graph/${type}`, param);
}
