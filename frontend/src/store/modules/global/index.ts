import { defineStore } from 'pinia';

import type { GlobalState, GlobalStateEvent } from './types';

const useGlobalStore = defineStore('global', {
  state: (): GlobalState => ({
    globalEvent: undefined,
  }),
  getters: {
    getGlobalEvent(state: GlobalState) {
      return state.globalEvent;
    },
  },
  actions: {
    dispatchGlobalEvent(event: GlobalStateEvent) {
      this.globalEvent = event;
    },
  },
});
export default useGlobalStore;
