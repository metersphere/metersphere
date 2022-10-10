import {get, post} from "metersphere-frontend/src/plugins/request"


export function getLicense() {
  return get('/license/validate')
}

export function getSystemUserSize() {
  return get('system/user/size')
}

export function saveLicense(data) {
  return post('/license/addValidLicense', data);
}
