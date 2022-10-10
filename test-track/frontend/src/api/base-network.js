import {socket} from "metersphere-frontend/src/plugins/request"

export function buildPagePath({pageNum, pageSize, path}) {
  return path + "/" + pageNum + "/" + pageSize;
}

export function buildListPagePath({pageNum, pageSize}) {
  return `list/${pageNum}/${pageSize}`;
}

export function baseSocket(url) {
  return socket('/websocket/' + url);
}
