import {baseGet, basePost} from "@/network/base-network";

export function getRelationshipGraph(id, type, callback) {
  return baseGet('/graph/relationship/graph/' + id + '/' + type, callback);
}


export function getGraphByCondition(relationshipType, param, callback) {
  return basePost('/graph/relationship/graph/condition/' + relationshipType, param, callback);
}
