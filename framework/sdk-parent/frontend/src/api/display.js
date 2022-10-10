import {get} from "../plugins/request";

export function getDisplayInfo() {
  return get('/display/info');
}

export function getSystemTheme() {
  return get('/system/theme')
}

