import { get, post } from 'metersphere-frontend/src/plugins/request';

export function getRelationshipGraph(id, type) {
  return get('/graph/relationship/graph/' + id + '/' + type);
}

export function getGraphByCondition(relationshipType, param) {
  return post('/graph/relationship/graph/condition/' + relationshipType, param);
}
