import {defineStore} from 'pinia';
import user from 'metersphere-frontend/src/store/modules/user';
import counter from './modules/couter';

let useUserStore = defineStore(user);
let useCounterStore = defineStore(counter);

const useStore = () => ({
  user: useUserStore(),
  count: useCounterStore(),
});


export {
  useUserStore,
  useCounterStore,
  useStore as default
}

