import get from "metersphere-frontend"

export function listUsers(page, size) {
  return get(`/samples/user-management/list/${page}/${size}`)
}
