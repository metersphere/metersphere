import {get, request} from 'metersphere-frontend/src/plugins/request';

export function getPluginPage() {
  return get('/plugin/list');
}

export function getPluginPageByName(name) {
  return get(`/plugin/list?name=${name}`);
}

export function delPluginById(pluginId) {
  return get(`/plugin/delete/${pluginId}`);
}

export function addPlugin(file) {
  let formData = new FormData();
  if (file) {
    formData.append("file", file);
  }
  let config = {
    method: 'POST',
    url: '/plugin/add',
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(config);
}
