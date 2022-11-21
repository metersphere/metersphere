import { post, get } from 'metersphere-frontend/src/plugins/request';

export function getScheduleByIdAndType(scheduleResourceID, taskType) {
  let url = '/api/schedule/get/' + scheduleResourceID + '/' + taskType;
  return get(url);
}

export function scheduleDisable(params) {
  return post('/api/schedule/enable', params);
}

export function scheduleUpdate(params) {
  return post('/api/schedule/update', params);
}
