import {post} from "metersphere-frontend/src/plugins/request"

export function saveLicense(data) {
  return post("/samples/license/save", data)
}


