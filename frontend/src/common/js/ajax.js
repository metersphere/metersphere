import {Message, MessageBox} from 'element-ui';
import axios from "axios";
import i18n from '../../i18n/i18n';
import {TokenKey} from "@/common/js/constants";
import {getCurrentProjectID, getCurrentWorkspaceId, getUUID} from "@/common/js/utils";

export function registerRequestHeaders() {
  axios.interceptors.request.use(config => {
    let user = JSON.parse(localStorage.getItem(TokenKey));
    if (user && user.csrfToken) {
      config.headers['CSRF-TOKEN'] = user.csrfToken;
    }
    // 包含 工作空间 项目的标识
    config.headers['WORKSPACE'] = getCurrentWorkspaceId();
    config.headers['PROJECT'] = getCurrentProjectID();
    return config;
  });
}

export function getUploadConfig(url, formData) {
  return {
    method: 'POST',
    url: url,
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
}


// 登入请求不重定向
let unRedirectUrls = new Set(['signin', 'ldap/signin', '/signin', '/ldap/signin']);
// session 是否掉线
let isSessionTimeout = false;

export function login() {
  if (isSessionTimeout) {
    // 已经弹出session掉线框
    return;
  }
  isSessionTimeout = true;
  MessageBox.alert(i18n.t('commons.tips'), i18n.t('commons.prompt'), {
    callback: () => {
      axios.get("/signout");
      localStorage.setItem('Admin-Token', "{}");
      window.location.href = "/login";
    }
  });
}

function then(success, response, result) {
  // 已经掉线不再弹出错误信息，避免满屏都是错误
  if (response && response.headers && response.headers["authentication-status"] === "invalid") {
    login();
    return;
  }
  if (!response.data) {
    success(response);
  } else if (response.data.success !== false) {
    success(response.data);
  } else {
    window.console.warn(response.data);
    if (response.data.message) {
      Message.warning(response.data.message);
    }
  }
  result.loading = false;
}

function exception(error, result, url) {
  if (error.response && error.response.status === 401 && !unRedirectUrls.has(url)) {
    login();
    return;
  }
  if (error.response && error.response.status === 403 && !unRedirectUrls.has(url)) {
    window.location.href = "/";
    return;
  }
  result.loading = false;
  window.console.error(error.response || error.message);
  if (error.response && error.response.data) {
    if (error.response.headers["authentication-status"] !== "invalid") {
      Message.error({message: error.response.data.message || error.response.data, showClose: true});
    }
  } else {
    Message.error({message: error.message, showClose: true});
  }
}

export function get(url, success) {
  let result = {loading: true};
  if (!success) {
    return axios.get(url);
  } else {
    axios.get(url).then(response => {
      then(success, response, result);
    }).catch(error => {
      exception(error, result, url);
    });
    return result;
  }
}

export function post(url, data, success, failure) {
  let result = {loading: true};
  if (!success) {
    return axios.post(url, data);
  } else {
    axios.post(url, data).then(response => {
      then(success, response, result);
    }).catch(error => {
      exception(error, result, url);
      if (failure) {
        then(failure, error, result);
      }
    });
    return result;
  }
}

export function request(axiosRequestConfig, success, failure) {
  let result = {loading: true};
  if (!success) {
    return axios.request(axiosRequestConfig);
  } else {
    axios.request(axiosRequestConfig).then(response => {
      then(success, response, result);
    }).catch(error => {
      exception(error, result);
      if (failure) {
        then(failure, error, result);
      }
    });
    return result;
  }
}



export function fileUpload(url, file, files, param, success, failure) {
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
  let axiosRequestConfig = getUploadConfig(url, formData);
  return request(axiosRequestConfig, success, failure);
}

export function download(config, fileName, success) {
  let result = {loading: true};
  this.$request(config).then(response => {
    doDownload(response.data, fileName);
    then(success, response, result);
  }).catch(error => {
    exception(error, result, "");
  });
  return result;
}

export function doDownload(content, fileName) {
  const blob = new Blob([content], {type: "application/octet-stream"});
  if ("download" in document.createElement("a")) {
    // 非IE下载
    //  chrome/firefox
    let aTag = document.createElement('a');
    aTag.download = fileName;
    aTag.href = URL.createObjectURL(blob);
    aTag.click();
    URL.revokeObjectURL(aTag.href);
  } else {
    // IE10+下载
    navigator.msSaveBlob(blob, fileName);
  }
}

import jsFileDownload from 'js-file-download'
import store from "@/store";
import {error} from "@/common/js/message";
export function downloadFile(method, url, data, fileName) {
  let downProgress = {};
  let id = getUUID();
  let config = {
    url: url,
    method: method,
    data: data,
    responseType: 'blob',
    headers: {"Content-Type": "application/json; charset=utf-8"},
    onDownloadProgress(progress) {
      // 计算出下载进度
      downProgress = Math.round(100 * progress.loaded / progress.total);
      // 下载进度存入 vuex
      store.commit('setDownloadFile', {id, 'progress': downProgress});
    }
  };
  axios.request(config)
    .then((res) => {
      fileName = fileName ? fileName : window.decodeURI(res.headers['content-disposition'].split('=')[1]);
      jsFileDownload(res.data, fileName);
  }).catch((e) => {
    error(e.message);
  })
}

export function fileDownload(url, fileName) {
  downloadFile('get', url, null, fileName);
}

export function fileDownloadPost(url, data, fileName) {
  downloadFile('post', url, data, fileName);
}

export function all(array, callback) {
  if (array.length < 1) return;
  axios.all(array).then(axios.spread(callback));
}

export default {
  install(Vue) {

    if (!axios) {
      window.console.error('You have to install axios');
      return;
    }

    if (!Message) {
      window.console.error('You have to install Message of ElementUI');
      return;
    }

    // let login = login;

    axios.defaults.withCredentials = true;

    axios.interceptors.response.use(response => {
      if (response.headers["authentication-status"] === "invalid") {
        login();
      }
      return response;
    }, error => {
      return Promise.reject(error);
    });

    Vue.prototype.$get = get;

    Vue.prototype.$post = post;

    Vue.prototype.$request = request;

    Vue.prototype.$all = all;

    Vue.prototype.$fileDownload = fileDownload;

    Vue.prototype.$fileDownloadPost = fileDownloadPost;

    Vue.prototype.$fileUpload = fileUpload;

    Vue.prototype.$download = download;
  }
};
