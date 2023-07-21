import { defineStore } from 'pinia';
import type { PluginState } from '@/models/setting/plugin';
import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';

const usePluginMangerStore = defineStore('pluginManger', {
  state: (): PluginState => ({
    doNotShowAgain: JSON.parse(getLocalStorage('doNotShowAgain') as string)?.value || false,
  }),
  getters: {
    getDoNotShowAgain(): boolean {
      return this.doNotShowAgain;
    },
  },
  actions: {
    setDoNotShowAgain(newValue: boolean): void {
      this.doNotShowAgain = newValue;
      setLocalStorage('doNotShowAgain', { value: newValue });
    },
  },
});

export default usePluginMangerStore;
