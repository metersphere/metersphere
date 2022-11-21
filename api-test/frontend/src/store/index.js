import user from 'metersphere-frontend/src/store/modules/user';
import { defineStore } from 'pinia';
import apiState from './state';
import performanceState from './modules/performance';

let useUserStore = defineStore(user);
let useApiStore = defineStore(apiState);
let usePerformanceStore = defineStore(performanceState);

const useStore = () => ({
  user: useUserStore(),
  api: useApiStore(),
  performance: usePerformanceStore(),
});

export { useUserStore, useApiStore, usePerformanceStore, useStore as default };
