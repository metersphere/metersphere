import {get} from '../plugins/request'


export function getApps() {
  return get('/services');
}
