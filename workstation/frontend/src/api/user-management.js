
import {get} from 'metersphere-frontend/src/plugins/request'

export function listUsers(page, size) {
  return get(`/samples/user-management/list/${page}/${size}`)
}
