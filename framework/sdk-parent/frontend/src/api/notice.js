import {post} from "../plugins/request"

export function saveNoticeTemplate(notice) {
  return post('/notice/template/save', notice);
}

