import {get, post} from 'metersphere-frontend/src/plugins/request'

export function batchModifyAppSetting(params) {
  return post('/project_application/update/batch', params);
}

export function getProjectAppSetting(projectId) {
  return get(`/project_application/get/config/${projectId}`);
}
