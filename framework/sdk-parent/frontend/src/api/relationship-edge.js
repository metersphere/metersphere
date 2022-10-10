import {post, get} from "../plugins/request";

const BASE_URL = "/relationship/edge/";

export function deleteRelationshipEdge(sourceId, targetId) {
  return get(BASE_URL + 'delete/' + sourceId + '/' + targetId);
}

