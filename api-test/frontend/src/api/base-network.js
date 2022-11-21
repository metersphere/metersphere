import { request, socket } from 'metersphere-frontend/src/plugins/request';
import jsFileDownload from 'js-file-download';
import { $error } from 'metersphere-frontend/src/plugins/message';

export function getUploadConfig(url, formData) {
  return {
    method: 'POST',
    url: url,
    data: formData,
    headers: {
      'Content-Type': undefined,
    },
  };
}

export function fileUpload(url, file, files, param) {
  let formData = new FormData();
  if (file) {
    formData.append('file', file);
  }
  if (files) {
    files.forEach((f) => {
      formData.append('files', f);
    });
  }
  formData.append('request', new Blob([JSON.stringify(param)], { type: 'application/json' }));
  let config = getUploadConfig(url, formData);
  return request(config);
}

export function fileDownload(url, fileName) {
  downloadFile('get', url, null, fileName);
}

export function downloadFile(method, url, data, fileName) {
  let config = {
    url: url,
    method: method,
    data: data,
    responseType: 'blob',
    headers: { 'Content-Type': 'application/json; charset=utf-8' },
  };
  request(config)
    .then((res) => {
      fileName = fileName ? fileName : window.decodeURI(res.headers['content-disposition'].split('=')[1]);
      jsFileDownload(res.data, fileName);
    })
    .catch((e) => {
      $error(e.message);
    });
}

export function baseSocket(url) {
  return socket('/websocket/' + url);
}

export function handleCtrlSEvent(event, func) {
  if (event.keyCode === 83 && event.ctrlKey) {
    func();
    event.preventDefault();
    event.returnValue = false;
    return false;
  }
}

export function handleCtrlREvent(event, func) {
  if (event.keyCode === 82 && event.ctrlKey) {
    func();
    event.preventDefault();
    event.returnValue = false;
    return false;
  }
}
