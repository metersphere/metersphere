import {getUUID} from "../utils";
import {get, request} from "../plugins/request"


export function uploadMarkDownImg(file) {
  let param = {
    id: getUUID().substring(0, 8)
  };
  file.prefix = param.id;
  param.fileName = file.name.substring(file.name.lastIndexOf('.'));
  let config = {
    method: 'POST',
    url: '/plugin/add',
    data: param,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(config);
}

export function deleteMarkDownImg(file) {
  return deleteMarkDownImg(file[1].name);
}

export function deleteMarkDownImgByName(name) {
  return get('/resource/md/delete/' + name);
}
