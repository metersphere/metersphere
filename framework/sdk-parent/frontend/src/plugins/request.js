import axios from 'axios'
import {$error} from "./message"
import {getCurrentProjectID, getCurrentWorkspaceId} from "../utils/token";
import {PROJECT_ID, TokenKey, WORKSPACE_ID, TASK_PATH, TASK_DATA} from "../utils/constants";
import packageJSON from '@/../package.json'
import {getUrlParams, getUUID} from "../utils";
import {initTaskData} from "../api/novice";
import {Base64} from "js-base64";

// baseURL 根据是否是独立运行修改
let baseURL = '';
if (window.__POWERED_BY_QIANKUN__) {
  baseURL = '/' + packageJSON.name
}

// url 中直接写了 module， eg: /setting
if (window.location.pathname.startsWith('/' + packageJSON.name)) {
  if (window.location.search.indexOf('shareId=') > -1) {
    baseURL = '/' + packageJSON.name;
  } else {
    window.location.href = '/'
  }
}

let urlParams = getUrlParams(window.location.href);

const instance = axios.create({
  baseURL, // url = base url + request url
  withCredentials: true,
  // timeout: 60000 // request timeout, default 1 min
})

// 每次请求加上Token。如果没用使用Token，删除这个拦截器
instance.interceptors.request.use(
  config => {
    let user = JSON.parse(localStorage.getItem(TokenKey));
    if (user && user.csrfToken) {
      config.headers['CSRF-TOKEN'] = user.csrfToken;
    }
    if (user && user.sessionId) {
      config.headers['X-AUTH-TOKEN'] = user.sessionId;
    }
    // sso callback 过来的会有sessionId在url上
    if (!config.headers['X-AUTH-TOKEN']) {
      let sessionId = urlParams['_token']
      if (sessionId) {
        config.headers['X-AUTH-TOKEN'] = Base64.decode(sessionId);
      }
    }
    // sso callback 过来的会有csrf在url上
    if (!config.headers['CSRF-TOKEN']) {
      let csrf = urlParams['_csrf']
      if (csrf) {
        config.headers['CSRF-TOKEN'] = csrf;
      }
    }
    // 包含 工作空间 项目的标识
    config.headers['WORKSPACE'] = getCurrentWorkspaceId();
    config.headers['PROJECT'] = getCurrentProjectID();
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

let language = localStorage.getItem('language');
if (!language) {
  language = navigator.language || navigator.browserLanguage;
}
instance.defaults.headers.common['Accept-Language'] = language;

function clearLocalStorage() {
  localStorage.removeItem(TokenKey);
  sessionStorage.removeItem(WORKSPACE_ID);
  sessionStorage.removeItem(PROJECT_ID);
  location.href = "/";
}

const checkAuth = response => {
  // 请根据实际需求修改
  if (!response.headers) {
    clearLocalStorage();
    return;
  }
  if (response.headers["authentication-status"] === "invalid") {
    clearLocalStorage();
  }
}

const checkPermission = response => {
  // 请根据实际需求修改
  if (response.status === 403) {
    location.href = "/";
  }
}

const checkTask = response => {
  // 请根据实际需求修改
  if (TASK_PATH.includes(response.config["url"]) && response.status === 200) {
    initTaskData(response.config["url"]);
  }
}

// 请根据实际需求修改
instance.interceptors.response.use(response => {
  checkAuth(response);
  checkTask(response);
  return response;
}, error => {
  let msg;
  if (error.response && error.response.headers) {
    // 仅处理 /is-login
    if (error.response.status === 401
      && error.response.data.success === false
      && error.response.request.responseURL.endsWith("/is-login")) {
      return Promise.reject(error.response.data);
    }

    // 判断错误标记
    if (error.response.status === 402) {
      if (error.response.headers['redirect']) {
        window.open(error.response.headers['redirect']);
      }
    }
    checkAuth(error.response);
    checkPermission(error.response);
    msg = error.response.data.message || error.response.data;
  } else {
    msg = error.message;
  }
  if (msg && !(msg instanceof Object)) {
    $error(msg)
  }
  return Promise.reject(error);
});

export const request = instance

/* 简化请求方法，统一处理返回结果，并增加loading处理，这里以{success,data,message}格式的返回值为例，具体项目根据实际需求修改 */
const promise = (request, loading = {}) => {
  return new Promise((resolve, reject) => {
    loading.status = true;
    request.then(response => {
      if (response.data.success) {
        resolve(response.data);
      } else {
        reject(response.data)
      }
      loading.status = false;
    }).catch(error => {
      reject(error)
      loading.status = false;
    })
  })
}

export function fileUpload(url, file, param) {
  return _fileUpload(url, file, null, param);
}

export function multiFileUpload(url, files, param) {
  return _fileUpload(url, null, files, param);
}

export function _fileUpload(url, file, files, param) {
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
  let axiosRequestConfig = {
    method: 'POST',
    url: url,
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return promise(request(axiosRequestConfig));
}

export function fileUploadWithProcessAndCancel(url, file, param, CancelTokenObj, cancelFileTokenList, progressCallback) {
  let progress = 0;
  let formData = new FormData();
  if (file) {
    formData.append("file", file);
  }
  formData.append('request', new Blob([JSON.stringify(param)], {type: "application/json"}));
  let axiosRequestConfig = {
    method: 'POST',
    url: url,
    data: formData,
    headers: {'Content-Type': 'application/json;charset=UTF-8'},
    cancelToken: new CancelTokenObj(function executor(c) {
      cancelFileTokenList.push({"name": file.name, "cancelFunc": c});
    }),
    onUploadProgress: progressEvent => { // 获取文件上传进度
      progress = (progressEvent.loaded / progressEvent.total * 100) | 0
      progressCallback({progress, status: progress})
    }
  };
  return promise(request(axiosRequestConfig));
}

export function fileDownloadGet(url, fileName, processHandler) {
  return downloadFile('get', url, null, fileName, processHandler);
}

export function fileDownloadPost(url, data, fileName, processHandler) {
  return downloadFile('post', url, data, fileName, processHandler);
}

export function downloadFile(method, url, data, fileName, processHandler) {
  let downProgress = {};
  let id = getUUID();
  let config = {
    url: url,
    method: method,
    data: data,
    responseType: 'blob',
    headers: {"Content-Type": "application/json; charset=utf-8"},
    onDownloadProgress(progress) {
      if (processHandler) {
        // 计算出下载进度
        downProgress = Math.round(100 * progress.loaded / progress.total);
        processHandler({id, 'progress': downProgress});
      }
    }
  };

  return new Promise((resolve, reject) => {
    request(config)
      .then((res) => {
        fileName = fileName ? fileName : window.decodeURI(res.headers['content-disposition'].split('=')[1]);
        fileName = fileName.replaceAll("\"", "");
        _downloadFile(fileName, res.data);
        resolve();
      }).catch((e) => {
        // 报错后，将 blob 格式转成字符串，打印错误信息
        let reader = new FileReader();
        reader.readAsText(e.response.data, 'utf-8');
        reader.onload = function (e) {
          if (reader.result) {
            let info = JSON.parse(reader.result);
            reject(info);
            $error(info.message);
          }
        }
      });
  });
}

export function _downloadFile(name, content) {
  const blob = new Blob([content]);
  if ("download" in document.createElement("a")) {
    // 非IE下载
    //  chrome/firefox
    let aTag = document.createElement('a');
    aTag.download = name;
    aTag.href = URL.createObjectURL(blob);
    aTag.click();
    URL.revokeObjectURL(aTag.href);
  } else {
    // IE10+下载
    navigator.msSaveBlob(blob, name);
  }
}

export const get = (url, data, loading) => {
  return promise(request({url: url, method: "get", params: data}), loading)
};

export const post = (url, data, loading) => {
  return promise(request({url: url, method: "post", data}), loading)
};

export const put = (url, data, loading) => {
  return promise(request({url: url, method: "put", data}), loading)
};

export const del = (url, loading) => {
  return promise(request({url: url, method: "delete"}), loading)
};

export const socket = (url) => {
  let protocol = "ws://";
  if (window.location.protocol === 'https:') {
    protocol = "wss://";
  }
  let uri = protocol + window.location.host + url;
  if (window.__POWERED_BY_QIANKUN__) {
    uri = protocol + window.location.host + "/" + packageJSON.name + url;
  }
  return new WebSocket(uri);
};

export const generateShareUrl = (name, shareUrl) => {
  if (window.__POWERED_BY_QIANKUN__) {
    return window.location.origin + '/' + packageJSON.name + name + shareUrl;
  } else {
    return window.location.origin + name + shareUrl;
  }
}

export const generateModuleUrl = (url) => {
  if (window.__POWERED_BY_QIANKUN__) {
    return window.location.origin + '/' + packageJSON.name + url;
  } else {
    return window.location.origin + url;
  }
}

export default {
  install(Vue) {
    Vue.prototype.$get = get;
    Vue.prototype.$post = post;
    Vue.prototype.$put = put;
    Vue.prototype.$delete = del;
    Vue.prototype.$request = request;
    Vue.prototype.$fileDownloadPost = fileDownloadPost;
    Vue.prototype.$fileDownload = fileDownloadGet;
    Vue.prototype.$fileUpload = fileUpload;
    Vue.prototype.$multiFileUpload = multiFileUpload;
  }
}
