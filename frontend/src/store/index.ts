import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import useAppStore from './modules/app';
import useVisitStore from './modules/app/visit';
import useUserStore from './modules/user';
import useTabBarStore from './modules/tab-bar';
import { debouncePlugin } from './plugins';

const pinia = createPinia().use(debouncePlugin).use(piniaPluginPersistedstate);

export { useAppStore, useUserStore, useTabBarStore, useVisitStore };
export default pinia;
