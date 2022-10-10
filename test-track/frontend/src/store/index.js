import user from 'metersphere-frontend/src/store/modules/user';
import common from 'metersphere-frontend/src/store/modules/common';
import {defineStore} from 'pinia';
import state from './state';

export const useUserStore = defineStore(user);
export const useStore = defineStore(state);
export const useCommonStore = defineStore(common);

