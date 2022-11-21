import { post, get } from "metersphere-frontend/src/plugins/request";

export function getMessageById(id) {
  let url = "/notice/search/message/" + id;
  return get(url);
}

export function delMessage(id) {
  let url = "/notice/delete/message/" + id;
  return get(url);
}

export function saveMessage(req) {
  return post("/notice/save/message/task", req);
}

export function saveTemplate(req) {
  return post("/notice/template/save", req);
}

export function saveNotice(req) {
  return post("/notice/save", req);
}
