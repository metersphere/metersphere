import {get, post, request} from 'metersphere-frontend/src/plugins/request';

export function getEnvironmentPages(goPage, pageSize, param) {
  return post(`/environment/list/${goPage}/${pageSize}`, param);
}

export function getEnvironment(id) {
  return get(`/environment/get/${id}`);
}

export function getEnvironments(projectId) {
  return get(`/environment/list/${projectId}`);
}

export function delEnvironmentById(envId) {
  return get(`/environment/delete/${envId}`);
}

export function addEnvironment(param) {
  let formData = new FormData();
  formData.append('request', new Blob([JSON.stringify(param)], {type: 'application/json'}));
  let config = {
    method: 'POST',
    url: '/environment/add',
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(config);
}

export function importEnvironment(params) {
  return post("/environment/import", params);
}

export function editEnv(formData, param) {
  let url = '/api/environment/add';
  if (param.id) {
    url = '/api/environment/update';
  }
  let config = getUploadConfig(url, formData);
  return request(config);
}

export function getUploadConfig(url, formData) {
  return {
    method: 'POST', url: url, data: formData, headers: {
      'Content-Type': undefined
    }
  };
}

// todo
export function environmentEntry(file, files, params) {
  let url = '/api/environment/get/entry';
  return fileUpload(url, file, files, params);
}

// todo
export function fileUpload(url, file, files, param) {
  let formData = new FormData();
  if (file) {
    formData.append("file", file);
  }
  if (files) {
    files.forEach(f => {
      formData.append("files", f);
    });
  }
  formData.append('request', new Blob([JSON.stringify(param)], {type: "application/json"}));
  let config = getUploadConfig(url, formData);
  return request(config);
}
