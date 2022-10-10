import user from './modules/user';
import {defineStore} from 'pinia';
import common from "./modules/common";

let useUserStore = defineStore(user);
let useCommonStore = defineStore(common);
const useStore = () => ({
  user: useUserStore()
});

export {
  useUserStore,
  useCommonStore,
  useStore as default
};
