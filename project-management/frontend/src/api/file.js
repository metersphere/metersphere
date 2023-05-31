import {fileDownloadGet, fileDownloadPost, get, post, request} from 'metersphere-frontend/src/plugins/request'

export function getFileMeta(projectId, userId) {
  return get(`/file/metadata/count/${projectId}/${userId}`);
}

export function modifyFileMeta(param) {
  return post('/file/metadata/update', param);
}

export function getFileBytes(id) {
  return get('/file/metadata/info/' + id);
}

export function pullGitFile(data) {
  let formData = new FormData();
  formData.append("request", new Blob([JSON.stringify(data)], {type: "application/json"}));
  let options = {
    method: 'POST',
    url: '/file/repository/git/pull',
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(options);
}

export function uploadFileMeta(file, data) {
  let formData = new FormData();
  let req = JSON.parse(JSON.stringify(data));
  req.tags = JSON.stringify(req.tags);
  formData.append("request", new Blob([JSON.stringify(req)], {type: "application/json"}));
  formData.append("file", file);
  let options = {
    method: 'POST',
    url: '/file/metadata/upload',
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(options);
}

export function createFileMeta(file, data) {
  let formData = new FormData();
  formData.append("request", new Blob([JSON.stringify(data)], {type: "application/json"}));
  formData.append("file", file);
  let options = {
    method: 'POST',
    url: '/file/metadata/create',
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(options);
}

export function editFileMeta(file, data) {
  let formData = new FormData();
  formData.append("request", new Blob([JSON.stringify(data)], {type: "application/json"}));
  formData.append("file", file);
  let options = {
    method: 'POST',
    url: '/file/metadata/create',
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(options);
}

export function downloadMetaData(id, name) {
  fileDownloadGet("/file/metadata/download/" + id, name);
}

export function downloadFileZip(request) {
  fileDownloadPost("/file/metadata/download/zip", request, "文件集.zip");
}

export function batchDeleteMetaData(ids) {
  return post('/file/metadata/delete/batch', ids);
}

export function deleteFileMetaById(metaId) {
  return get(`/file/metadata/delete/${metaId}`);
}

export function getAllTypeFileMeta() {
  return get('/file/metadata/get/type/all');
}

export function getFileMetaPages(projectId, goPage, pageSize, condition) {
  return post(`/file/metadata/project/${projectId}/${goPage}/${pageSize}`, condition);
}

export function moveFileMeta(request) {
  return post('/file/metadata/move', request);
}

export function getFileModule(projectId) {
  return get(`/file/module/list/${projectId}`);
}

export function modifyFileModule(param) {
  return post('/file/module/edit', param);
}

export function createFileModule(module) {
  return post('/file/module/add', module);
}

export function deleteFileModule(ids) {
  return post('/file/module/delete', ids);
}

export function dragModule(param) {
  return post('/file/module/drag', param);
}

export function posModule(param) {
  return post('/file/module/pos', param);
}

export function testConnectRepository(param) {
  return post('/file/repository/connect', param);
}

export function getFileVersion(param) {
  return get('/file/repository/fileVersion/' + param);
}

export function getRemoteFileRelevanceCase(refId, pageIndex, pageSize, param) {
  return post('/file/repository/relevance/case/' + refId + "/" + pageIndex + "/" + pageSize, param);
}

export function updateRemoteFileRelevanceCaseVersion(refId, param) {
  return post('/file/repository/case/version/update/' + refId, param);
}
