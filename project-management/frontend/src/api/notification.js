import {get, post} from 'metersphere-frontend/src/plugins/request';

export function searchNoticeByType(type) {
  return get(`/notice/search/message/type/${type}`);
}

export function searchNoticeById(id) {
  return get(`/notice/search/message/${id}`);
}

export function saveNoticeTask(task) {
  return post('/notice/save/message/task', task);
}

export function deleteNoticeTask(taskId) {
  return get(`/notice/delete/message/${taskId}`);
}

export function searchTemplateById(id, event) {
  return get(`/notice/search/template/${id}/${event}`);
}
