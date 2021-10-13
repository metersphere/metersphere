import {baseGet} from "@/network/base-network";

export function deleteRelationshipEdge(sourceId, targetId, callback) {
  return baseGet('/relationship/edge/delete/' + sourceId + '/' + targetId, callback);
}

