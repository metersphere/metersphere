import {post, get, fileUpload} from "@/business/utils/sdk-utils";
import {buildPagePath} from "@/api/base-network";

const BASE_URL = '/api/definition/';

export function apiDefinitionListBatch(param) {
  return post(BASE_URL + 'list/batch', param);
}

export function apiDefinitionRelevance(param) {
  return post(BASE_URL + 'relevance', param);
}

export function apiDefinitionListRelevance(page, param) {
  page.path = 'list/relevance';
  return post(BASE_URL + buildPagePath(page), param);
}

export function apiDefinitionGet(id) {
  return get(BASE_URL + `get/${id}`);
}

export function apiDefinitionReportGet(id) {
  return get(BASE_URL + `report/get/${id}`);
}

export function apiDefinitionReportGetDb(id) {
  return get(BASE_URL + `report/getReport/${id}`);
}

export function apiDefinitionRunDebug(file, files, params) {
  return fileUpload(BASE_URL + 'run/debug', file, files, params);
}
