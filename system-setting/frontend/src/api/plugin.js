import {get, request} from 'metersphere-frontend/src/plugins/request';

export function getPluginPage() {
  return get('/plugin/list');
}

export function getPluginPageByName(name) {
  return get(`/plugin/list?name=${name}`);
}

export function delPluginById(scenario, pluginId) {
  return get(`/plugin/delete/${scenario}/${pluginId}`);
}

export function getPluginById(pluginId) {
  return get(`/plugin/get/${pluginId}`);
}

export function addPlugin(scenario, file) {
  let formData = new FormData();
  if (file) {
    formData.append("file", file);
  }
  let url = '/plugin/add/' + scenario;
  let config = {
    method: 'POST',
    url:  url,
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(config);
}
