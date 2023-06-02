import { App, ComponentPublicInstance } from 'vue';
import axios from 'axios';

/**
 * TODO:错误捕获
 * @param Vue
 * @param baseUrl
 * @returns
 */
export default function handleError(Vue: App, baseUrl: string) {
  if (!baseUrl) {
    return;
  }
  Vue.config.errorHandler = (err: unknown, instance: ComponentPublicInstance | null, info: string) => {
    // send error info
    axios.post(`${baseUrl}/report-error`, {
      err,
      instance,
      info,
      // location: window.location.href,
      // message: err.message,
      // stack: err.stack,
      // browserInfo: getBrowserInfo(),
      // user info
      // dom info
      // url info
      // ...
    });
  };
}
