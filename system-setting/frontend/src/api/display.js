import {get, request} from "metersphere-frontend/src/plugins/request";

export function getDisplayInfo() {
  return get('/display/info');
}

export function getSystemTheme() {
  return get('/system/theme')
}

export function saveDisplay(formData) {
  let options = {
    method: 'POST',
    url: "/display/save",
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(options)
}
