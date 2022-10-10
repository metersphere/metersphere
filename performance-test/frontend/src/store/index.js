import user from 'metersphere-frontend/src/store/modules/user';
import {defineStore} from 'pinia';

let useUserStore = defineStore(user);

const useStore = () => ({
  user: useUserStore(),
});

export {
  useUserStore,
  useStore as default
};
