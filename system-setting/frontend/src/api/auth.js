import {get, post} from "metersphere-frontend/src/plugins/request"

export function getAuth(id) {
  return get('/authsource/get/' + id)
}

export function changeStatus(id, status) {
  return get('/authsource/update/' + id + '/status/' + status)
}

export function delAuth(id) {
  return get(`/authsource/delete/` + id)
}

export function addAuth(data) {
  return post("/authsource/add", data);
}

export function updateAuth(data) {
  return post("/authsource/update", data)
}

export function getAllEnable() {
  return get('authsource/list/allenable')
}

export function searchAuths(data, currentPage, pageSize) {
  return post(`authsource/list/${currentPage}/${pageSize}`, data);
}
