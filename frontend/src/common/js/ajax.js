import {Message, MessageBox} from 'element-ui';
import axios from "axios";
import i18n from '../../i18n/i18n'


export default {
  install(Vue) {
    if (!axios) {
      window.console.error('You have to install axios');
      return
    }

    if (!Message) {
      window.console.error('You have to install Message of ElementUI');
      return
    }

    let login = function () {
      MessageBox.alert(i18n.t('commons.tips'), i18n.t('commons.prompt'), {
        callback: () => {
          axios.get("/signout");
          localStorage.setItem('Admin-Token', "{}");
          window.location.href = "/login"
        }
      });
    };

    axios.defaults.withCredentials = true;

    axios.interceptors.response.use(response => {
      if (response.headers["authentication-status"] === "invalid") {
        login();
      }
      return response;
    }, error => {
      return Promise.reject(error);
    });

    function then(success, response, result) {
      if (!response.data) {
        success(response);
      } else if (response.data.success) {
        success(response.data);
      } else {
        window.console.warn(response.data);
        Message.warning(response.data.message);
      }
      result.loading = false;
    }

    function exception(error, result) {
      if (error.response && error.response.status === 401) {
        login();
        return;
      }
      result.loading = false;
      window.console.error(error.response || error.message);
      if (error.response && error.response.data) {
        if (error.response.headers["authentication-status"] !== "invalid") {
          Message.error({message: error.response.data.message, showClose: true});
        }
      } else {
        Message.error({message: error.message, showClose: true});
      }
    }

    Vue.prototype.$$get = function (url, data, success) {
      let result = {loading: true};
      if (!success) {
        return axios.get(url, {params: data});
      } else {
        axios.get(url, {params: data}).then(response => {
          then(success, response, result);
        }).catch(error => {
          exception(error, result);
        });
        return result;
      }
    };

    Vue.prototype.$get = function (url, success) {
      let result = {loading: true};
      if (!success) {
        return axios.get(url);
      } else {
        axios.get(url).then(response => {
          then(success, response, result);
        }).catch(error => {
          exception(error, result);
        });
        return result;
      }
    };

    Vue.prototype.$post = function (url, data, success, failure) {
      let result = {loading: true};
      if (!success) {
        return axios.post(url, data);
      } else {
        axios.post(url, data).then(response => {
          then(success, response, result);
        }).catch(error => {
          exception(error, result);
          if (failure) {
            then(failure, error, result);
          }
        });
        return result;
      }
    };

    Vue.prototype.$request = function (axiosRequestConfig, success, failure) {
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
    };

    Vue.prototype.$all = function (array, callback) {
      if (array.length < 1) return;
      axios.all(array).then(axios.spread(callback));
    };

    Vue.prototype.$fileDownload = function (url) {
      axios.get(url, {responseType: 'blob'})
        .then(response => {
          let fileName = window.decodeURI(response.headers['content-disposition'].split('=')[1]);
          let link = document.createElement("a");
          link.href = window.URL.createObjectURL(new Blob([response.data], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8"}));
          link.download = fileName;
          link.click();
        });
    };

    Vue.prototype.$fileUpload = function (url, file, files, param, success, failure) {
      let formData = new FormData();
      if (file) {
        formData.append("file", file);
      }
      if (files) {
        files.forEach(f => {
          formData.append("files", f);
        })
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
      return Vue.prototype.$request(axiosRequestConfig, success, failure);
    }

  }
}
