import { get, post, request } from '../plugins/request';
// 获取使用当前js模块的package.json，不要修改引入路径
import packageInfo from '@/../package.json';
const currentModuleName = packageInfo.name;

export function getEnvironmentMapByGroupId(id) {
  return get('/environment/group/map/' + id);
}

export function getEnvironmentByProjectId(projectId) {
  return get('/environment/list/' + projectId);
}
// 不含环境的blob数据
export function getEnvironmentByProjectIds(projectIds) {
  return post('/environment/project-env', projectIds);
}

export function getEnvironmentById(environmentId) {
  return get('/environment/get/' + environmentId);
}

export function environmentEntry(file, files, params) {
  let url = '/environment/get/entry';
  return fileUpload(url, file, files, params);
}

export function copyEnvironment(environmentId) {
  return get('/environment/group/copy/' + environmentId);
}

export function delEnvironment(environmentId) {
  return get('/environment/group/delete/' + environmentId);
}

export function environmentGroupList(envGroupId) {
  return get('/environment/group/project/list/' + envGroupId);
}

export function delApiEnvironment(id) {
  return get('/environment/delete/' + id);
}

export function environmentList(id) {
  return get('/environment/list/' + id);
}

export function envGroupList(page, pageSize, params) {
  return post('/environment/group/list/' + page + '/' + pageSize, params);
}

export function addGroupEnvironment(params) {
  return post('/environment/group/add', params);
}

export function environmentGetALL() {
  return post('/environment/group/get/all', {});
}

export function getEnvironmentOptions(params) {
  return post('/environment/group/get/option', params);
}

export function environmentGroupModify(params) {
  return post('/environment/group/modify', params);
}

export function editEnv(formData, param) {
  let url = '/environment/add';
  if (param.id) {
    url = '/environment/update';
  }
  let config = getUploadConfig(url, formData);
  return request(config);
}

export function getApiModuleByProjectIdAndProtocol(projectId, protocol) {
  let url = '/environment/module/list/' + projectId + '/' + protocol;
  return get(url);
}

export function definitionRawToXML(params) {
  return post('/environment/raw-to-xml', params);
}

export function databaseValidate(params) {
  return post('/environment/database/validate', params);
}

export function getUploadConfig(url, formData) {
  return {
    method: 'POST',
    url: url,
    data: formData,
    headers: {
      'Content-Type': undefined,
    },
  };
}

export function fileUpload(url, file, files, param) {
  let formData = new FormData();
  if (file) {
    formData.append('file', file);
  }
  if (files) {
    files.forEach((f) => {
      formData.append('files', f);
    });
  }
  formData.append('request', new Blob([JSON.stringify(param)], { type: 'application/json' }));
  let config = getUploadConfig(url, formData);
  return request(config);
}

// 环境中自定义代码片段使用的接口
export function getApiCaseById(id) {
  // 如果是API模块调用接口，不使用服务间调用
  if (currentModuleName === 'api') {
    return get('/api/testcase/get/' + id);
  }
  return get('/environment/relate/api/testcase/get/' + id);
}

export function getApiDocument(id, type) {
  if (currentModuleName === 'api') {
    return get('/api/definition/document/' + id + '/' + type);
  }
  return get('/environment/relate/api/definition/document/' + id + '/' + type);
}

export function batchGetApiDefinition(obj) {
  if (currentModuleName === 'api') {
    return post('/api/definition/list/batch', obj);
  }
  return post('/environment/relate/api/definition/list/batch', obj);
}

export function jsonGenerator(params) {
  if (currentModuleName === 'api') {
    return post('/api/definition/generator', params);
  }
  return post('/environment/relate/api/definition/generator', params);
}

export function getUserDefaultApiType() {
  if (currentModuleName === 'api') {
    return get('/api/module/default-type');
  }
  return get('/environment/relate/api/module/default-type');
}

export function getModuleByUrl(url) {
  if (currentModuleName === 'api') {
    return get(url);
  }
  return get('/environment/relate' + url);
}

export function getCaseRelateModuleByCondition(url, params) {
  return post('/environment/relate' + url, params);
}

export function getCodeSnippetPages(goPage, pageSize, params) {
  if (currentModuleName === 'project') {
    return post(`/custom/func/list/${goPage}/${pageSize}`, params);
  }
  return post(`/environment/relate/custom/func/list/${goPage}/${pageSize}`, params);
}

export function getCodeSnippetById(id) {
  if (currentModuleName === 'project') {
    return get(`/custom/func/get/${id}`);
  }
  return get(`/environment/relate/custom/func/get/${id}`);
}

export function getApiDefinitionPages(goPage, pageSize, param) {
  if (currentModuleName === 'api') {
    return post(`api/definition/list/${goPage}/${pageSize}`, param);
  }
  return post(`/environment/relate/api/definition/list/${goPage}/${pageSize}`, param);
}

export function getApiDefinitionById(id) {
  if (currentModuleName === 'api') {
    return get(`/api/definition/get/${id}`);
  }
  return get(`/environment/relate/api/definition/get/${id}`);
}

export function getApiTestCasePages(goPage, pageSize, param) {
  if (currentModuleName === 'api') {
    return post(`/api/testcase/list/${goPage}/${pageSize}`, param);
  }
  return post(`/environment/relate/api/testcase/list/${goPage}/${pageSize}`, param);
}

export function getTestCaseBLOBs(params) {
  if (currentModuleName === 'api') {
    return post('/api/testcase/list-blobs', params);
  }
  return post('/environment/relate/api/testcase/list-blobs', params);
}
//
