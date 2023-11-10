import { createPinia } from 'pinia';

import useTableStore from '@/hooks/useTableStore';

import useAppStore from './modules/app';
import useVisitStore from './modules/app/visit';
import useMinderStore from './modules/components/minder-editor';
import useUserStore from './modules/user';
import { debouncePlugin } from './plugins';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';

const pinia = createPinia().use(debouncePlugin).use(piniaPluginPersistedstate);

export { useAppStore, useMinderStore, useTableStore, useUserStore, useVisitStore };
export default pinia;
