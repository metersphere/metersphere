import {get, post} from "../plugins/request"
import {fileUpload} from "./environment";

export function getFileMetadata(id) {
  let url = '/file/metadata/exist/' + id;
  return get(url);
}

export function getFileModules(projectId) {
  let url = '/file/module/list/' + projectId;
  return get(url);
}

export function getTypeNodeByProjectId(projectId, moduleType) {
  let url = '/file/module/type/list/' + projectId + "/" + moduleType;
  return get(url);
}

export function getMetadataTypes() {
  let url = '/file/metadata/get/type/all';
  return get(url);
}

export function getFileMetadataList(projectId, currentPage, pageSize, req) {
  let url = '/file/metadata/project/' + projectId + '/' + currentPage + '/' + pageSize;
  return post(url, req);
}

export function fileExists(ids) {
  return post('/file/metadata/exists', ids);
}

export function dumpFile(file, files, params) {
  let url = '/file/metadata/dump/file';
  return fileUpload(url, file, files, params);
}
