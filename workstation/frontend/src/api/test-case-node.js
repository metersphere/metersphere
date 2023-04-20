import {post,get} from "metersphere-frontend/src/plugins/request";

export function getTestCaseNodes(projectId) {
  return get('/case/node/list/' + projectId);
}
