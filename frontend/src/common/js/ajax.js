import {Message, MessageBox} from 'element-ui';
import axios from "axios";

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
      MessageBox.alert("认证信息已过期，请重新登录。", {
        callback: () => {
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
      }
      if (response.data.success) {
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
      if (error.response.data) {
        Message.error({message: error.response.data.message, showClose: true});
      } else {
        Message.error({message: error.message, showClose: true});
      }
    }

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

    Vue.prototype.$post = function (url, data, success) {
      let result = {loading: true};
      if (!success) {
        return axios.post(url, data);
      } else {
        axios.post(url, data).then(response => {
          then(success, response, result);
        }).catch(error => {
          exception(error, result);
        });
        return result;
      }
    };

    Vue.prototype.$request = function (axiosRequestConfig, success) {
      let result = {loading: true};
      if (!success) {
        return axios.request(axiosRequestConfig);
      } else {
        axios.request(axiosRequestConfig).then(response => {
          then(success, response, result);
        }).catch(error => {
          exception(error, result);
        });
        return result;
      }
    };

    Vue.prototype.$all = function (array, callback) {
      if (array.length < 1) return;
      axios.all(array).then(axios.spread(callback));
    };
  }
}
