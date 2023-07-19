import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import useAppStore from './modules/app';
import useVisitStore from './modules/app/visit';
import useUserStore from './modules/user';
import { debouncePlugin } from './plugins';
import useUserGroupStore from './modules/setting/usergroup';
import useTableStore from './modules/ms-table';

const pinia = createPinia().use(debouncePlugin).use(piniaPluginPersistedstate);

export { useAppStore, useUserStore, useVisitStore, useUserGroupStore, useTableStore };
export default pinia;
