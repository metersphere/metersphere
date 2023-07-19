import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import useAppStore from './modules/app';
import useVisitStore from './modules/app/visit';
import useUserStore from './modules/user';
import useTableStore from './modules/ms-table';
import { debouncePlugin } from './plugins';

const pinia = createPinia().use(debouncePlugin).use(piniaPluginPersistedstate);

export { useAppStore, useUserStore, useVisitStore, useTableStore };
export default pinia;
