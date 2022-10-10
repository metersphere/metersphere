import {post, get} from "metersphere-frontend/src/plugins/request";
const BASE_URL = "/custom/field/";

export function getCustomField(id) {
  return get(BASE_URL + `get/${id}`);
}
