import {Message, MessageBox} from 'element-ui';
import axios from "axios";
import i18n from '../../i18n/i18n';
import {TokenKey} from "@/common/js/constants";

export function registerRequestHeaders() {
  axios.interceptors.request.use(config => {
    let user = JSON.parse(localStorage.getItem(TokenKey));
    if (user && user.csrfToken) {
      config.headers['CSRF-TOKEN'] = user.csrfToken;
    }
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

export function login() {
  MessageBox.alert(i18n.t('commons.tips'), i18n.t('commons.prompt'), {
    callback: () => {
      axios.get("/signout");
      localStorage.setItem('Admin-Token', "{}");
      window.location.href = "/login";
    }
  });
}

function then(success, response, result) {
  if (!response.data) {
    success(response);
  } else if (response.data.success) {
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

export function fileDownload(url) {
  axios.get(url, {responseType: 'blob'})
    .then(response => {
      let fileName = window.decodeURI(response.headers['content-disposition'].split('=')[1]);
      let link = document.createElement("a");
      link.href = window.URL.createObjectURL(new Blob([response.data], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8"}));
      link.download = fileName;
      link.click();
    });
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

    Vue.prototype.$fileUpload = fileUpload;
  }
};
