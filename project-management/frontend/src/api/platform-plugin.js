import {post, get, generateModuleUrl} from "metersphere-frontend/src/plugins/request";
const BASE_URL = "/platform/plugin/";

export function getPlatformProjectInfo(key) {
  return key ? get(BASE_URL + `project/info/${key}`) : new Promise(r => r({}));
}

export function validateProjectConfig(pluginId, config) {
  return post(BASE_URL + `project/validate/${pluginId}`, config);
}

export function getPlatformProjectOption(pluginId, request) {
  return post(BASE_URL + 'project/option', request);
}

export function getPlatformOption() {
  return get(BASE_URL + 'platform/option');
}

export function getThirdPartTemplateSupportPlatform() {
  return get(BASE_URL + 'template/support/list');
}

export function generatePlatformResourceUrl(configId, fileName) {
  return generateModuleUrl(BASE_URL + `resource/${configId}?fileName=${fileName}`);
}
