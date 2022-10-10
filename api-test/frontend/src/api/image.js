import {get, post} from "metersphere-frontend/src/plugins/request"

export function uploadMarkDownImg(file) {

}

export function deleteMarkDownImg(file) {
  if (file) {
    return get('/resource/md/delete/' + file[1].name);
  }
  return {};
}
