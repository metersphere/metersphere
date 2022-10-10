import {socket} from "metersphere-frontend/src/plugins/request"


export function baseSocket(url) {
  return socket('/websocket/' + url);
}
