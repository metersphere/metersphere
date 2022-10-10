import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/environment/group/';

export function environmentGroupGet(id) {
  return get(BASE_URL + `map/name/${id}`);
}

export function environmentGroupGetProjectMapName(id) {
  return get(BASE_URL + `project/map/name/${id}`);
}
