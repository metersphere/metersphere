import {get} from "../plugins/request"


export function getLicense() {
  return get('/license/validate')
}

export function getSystemUserSize() {
  return get('system/user/size')
}
