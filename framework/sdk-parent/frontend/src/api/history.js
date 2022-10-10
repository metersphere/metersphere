import {post} from "../plugins/request"

export function getOperatingLogForResource(goPage, pageSize, data) {
  return post(`/operating/log/get/source/${goPage}/${pageSize}`, data)
}
