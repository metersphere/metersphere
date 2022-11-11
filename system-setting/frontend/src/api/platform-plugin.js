import {post, get} from "metersphere-frontend/src/plugins/request";
const BASE_URL = "/platform/plugin/";

export function getIntegrationInfo() {
  return get(BASE_URL + 'integration/info');
}
