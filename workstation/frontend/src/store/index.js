import user from 'metersphere-frontend/src/store/modules/user';
import {defineStore} from 'pinia';
import apiState from './state';
let useApiStore = defineStore(apiState);


let useUserStore = defineStore(user);
const useStore = () => ({
  user: useUserStore(),
  api: useApiStore(),
});

export {
  useUserStore,
  useApiStore,
  useStore as default
};
