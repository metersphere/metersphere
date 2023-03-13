import {post} from "metersphere-frontend/src/plugins/request"

export function searchTests(goPage, pageSize, condition) {
  return post(`/performance/list/${goPage}/${pageSize}`, condition)
}
