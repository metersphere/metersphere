import {get, post, socket} from 'metersphere-frontend/src/plugins/request'

export function getCodeSnippetPages(goPage, pageSize, params) {
  return post(`/custom/func/list/${goPage}/${pageSize}`, params);
}

export function deleteCodeSnippetById(id) {
  return get(`/custom/func/delete/${id}`);
}

export function copyCodeSnippetById(id) {
  return get(`/custom/func/copy/${id}`);
}

export function getCodeSnippetById(id) {
  return get(`/custom/func/get/${id}`);
}

export function saveCodeSnippet(obj) {
  return post('/custom/func/save', obj);
}

export function modifyCodeSnippet(obj) {
  return post('/custom/func/update', obj);
}

export function runCodeSnippet( param) {
  return post(`/custom/func/run`, param);
}


export const apiSocket = (url) => {
  let protocol = "ws://";
  if (window.location.protocol === 'https:') {
    protocol = "wss://";
  }
  let uri = protocol + window.location.host + url;
  return new WebSocket(uri);
};

export function getSocket(reportId) {
  return apiSocket(`/api/websocket/${reportId}`)
}
