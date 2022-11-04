import {get, post, socket} from "../plugins/request"
import {getCurrentUserId} from "../utils/token";

export function getWsMemberList() {
  return get('/user/ws/current/member/list')
}

export function readAll() {
  return get('/notification/read/all')
}

export function read(id) {
  return get(`/notification/read/${id}`)
}

export function searchNotifications(data, goPage, pageSize) {
  return post(`/notification/list/all/${goPage}/${pageSize}`, data);
}

export function updateUserByResourceId(resourceId) {
  return get(`/user/update/current-by-resource/${resourceId}`);
}

export function initNoticeSocket() {
  let userId = getCurrentUserId();
  return socket(`/websocket/notification/count/${userId}`);
}

export function searchNoticeByType(type) {
  return get(`/notice/search/message/type/${type}`);
}

export function searchNoticeById(id) {
  return get(`/notice/search/message/${id}`);
}

export function saveNoticeTask(task) {
  return post('/notice/save/message/task', task);
}

export function updateNoticeTask(task) {
  return post('/notice/update/message/task', task);
}

export function deleteNoticeTask(taskId) {
  return get(`/notice/delete/message/${taskId}`);
}
