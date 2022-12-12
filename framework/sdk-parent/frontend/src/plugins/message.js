import {Message, MessageBox} from 'element-ui';
import i18n from "../i18n";

export const $alert = (message, callback, options) => {
  let title = i18n.t("commons.message_box.alert");
  MessageBox.alert(message, title, options).then(() => {
    callback();
  });
}

export const $confirm = (message, callback, options = {}) => {
  let defaultOptions = {
    confirmButtonText: i18n.t("commons.button.ok"),
    cancelButtonText: i18n.t("commons.button.cancel"),
    type: 'warning',
    ...options
  }
  let title = i18n.t("commons.message_box.confirm");
  MessageBox.confirm(message, title, defaultOptions).then(() => {
    callback();
  });
}

export const $success = (message, close, duration) => {
  Message.success({
    message: message,
    type: "success",
    showClose: close ?? true,
    duration: duration || 1500,
    customClass: "ms-custom-message-class"
  })
}

export const $info = (message, close, duration) => {
  Message.info({
    message: message,
    type: "info",
    showClose: close ?? true,
    duration: duration || 3000,
    customClass: "ms-custom-message-class"
  })
}

export const $warning = (message, close, duration) => {
  Message.warning({
    message: message,
    type: "warning",
    showClose: close ?? true,
    duration: duration || 5000,
    customClass: "ms-custom-message-class"
  })
}

export const $error = (message, close, duration) => {
  Message.error({
    message: message,
    type: "error",
    showClose: close ?? true,
    duration: duration || 10000,
    customClass: "ms-custom-message-class"
  })
}

export default {
  install(Vue) {
    // 使用$$前缀，避免与Element UI的冲突
    Vue.prototype.$$confirm = $confirm;
    Vue.prototype.$$alert = $alert;

    Vue.prototype.$success = $success;
    Vue.prototype.$info = $info;
    Vue.prototype.$warning = $warning;
    Vue.prototype.$error = $error;
  }
}
