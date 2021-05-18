import {getUUID} from "@/common/js/utils";
import {getUploadConfig, request} from "@/common/js/ajax";

function buildBodyFile(item, bodyUploadFiles, obj, bodyParam) {
  if (bodyParam) {
    bodyParam.forEach(param => {
      if (param.files) {
        param.files.forEach(fileItem => {
          if (fileItem.file) {
            fileItem.name = fileItem.file.name;
            obj.bodyFileRequestIds.push(item.id);
            bodyUploadFiles.push(fileItem.file);
          }
        });
      }
    });
  }
}

function setFiles(item, bodyUploadFiles, obj) {
  if (item.body) {
    buildBodyFile(item, bodyUploadFiles, obj, item.body.kvs);
    buildBodyFile(item, bodyUploadFiles, obj, item.body.binary);
  }
}

function recursiveFile(arr, bodyUploadFiles, obj) {
  arr.forEach(item => {
    setFiles(item, bodyUploadFiles, obj);
    if (item.hashTree !== undefined && item.hashTree.length > 0) {
      recursiveFile(item.hashTree, bodyUploadFiles, obj);
    }
  });
}

export function getBodyUploadFiles(obj, scenarioDefinition) {
  let bodyUploadFiles = [];
  obj.bodyFileRequestIds = [];
  scenarioDefinition.forEach(item => {
    setFiles(item, bodyUploadFiles, obj);
    if (item.hashTree !== undefined && item.hashTree.length > 0) {
      recursiveFile(item.hashTree, bodyUploadFiles, obj);
    }
  })
  return bodyUploadFiles;
}

function getScenarioFiles(obj) {
  let scenarioFiles = [];
  obj.scenarioFileIds = [];
  // 场景变量csv 文件
  if (obj.variables) {
    obj.variables.forEach(param => {
      if (param.type === 'CSV' && param.files) {
        param.files.forEach(item => {
          if (item.file) {
            if (!item.id) {
              let fileId = getUUID().substring(0, 12);
              item.name = item.file.name;
              item.id = fileId;
            }
            obj.scenarioFileIds.push(item.id);
            scenarioFiles.push(item.file);
          }
        })
      }
    });
  }
  return scenarioFiles;
}

export function saveScenario(url, scenario, scenarioDefinition, success) {
  let bodyFiles = getBodyUploadFiles(scenario, scenarioDefinition);
  let scenarioFiles = getScenarioFiles(scenario);
  let formData = new FormData();
  if (bodyFiles) {
    bodyFiles.forEach(f => {
      formData.append("bodyFiles", f);
    })
  }
  if (scenarioFiles) {
    scenarioFiles.forEach(f => {
      formData.append("scenarioFiles", f);
    })
  }
  formData.append('request', new Blob([JSON.stringify(scenario)], {type: "application/json"}));
  let axiosRequestConfig = getUploadConfig(url, formData);
  request(axiosRequestConfig, (response) => {
    if (success) {
      success(response);
    }
  });
}
