import {post, get} from "metersphere-frontend/src/plugins/request";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";

const BASE_URL = "/project_application/";

export function getProjectApplicationConfig(type, projectId) {
  projectId = projectId ? projectId : getCurrentProjectID();
  return get(BASE_URL + `get/${projectId}/${type}`);
}
