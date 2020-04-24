import {Message} from 'element-ui';

export default {
  install(Vue) {
    if (!Message) {
      window.console.error('You have to install Message of ElementUI');
      return
    }

    Vue.prototype.$success = function (message) {
      Message.success({
        message: message,
        type: "success",
        showClose: true,
        duration: 1000
      })
    };

    Vue.prototype.$info = function (message) {
      Message.info({
        message: message,
        type: "info",
        showClose: true,
        duration: 3000
      })
    };

    Vue.prototype.$warning = function (message) {
      Message.warning({
        message: message,
        type: "warning",
        showClose: true,
        duration: 2000
      })
    };

    Vue.prototype.$error = function (message) {
      Message.error({
        message: message,
        type: "error",
        showClose: true,
        duration: 10000
      })
    };


  }
}
