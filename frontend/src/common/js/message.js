import {Message} from 'element-ui';

export function success(message) {
  Message.success({
    message: message,
    type: "success",
    showClose: true,
    duration: 1500
  });
}

export function info(message, duration) {
  Message.info({
    message: message,
    type: "info",
    showClose: true,
    duration: duration || 3000
  });
}

export function warning(message) {
  Message.warning({
    message: message,
    type: "warning",
    showClose: true,
    duration: 5000
  });
}

export function error(message, duration) {
  Message.error({
    message: message,
    type: "error",
    showClose: true,
    duration: duration || 10000
  });
}

export default {
  install(Vue) {
    if (!Message) {
      window.console.error('You have to install Message of ElementUI');
      return
    }

    Vue.prototype.$success = success;

    Vue.prototype.$info = info;

    Vue.prototype.$warning = warning;

    Vue.prototype.$error = error;
  }
}
