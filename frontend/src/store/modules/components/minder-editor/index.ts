import { defineStore } from 'pinia';

import { MinderNodePosition, MinderState } from './types';

// 脑图组件的 store
const useMinderStore = defineStore('minder', {
  state: (): MinderState => ({
    event: {
      name: '',
      timestamp: 0,
      nodePosition: {
        x: 0,
        y: 0,
      },
      nodeDom: undefined,
      nodeData: undefined,
    },
    mold: 0,
  }),
  actions: {
    dispatchEvent(name: string, position: MinderNodePosition, nodeDom?: HTMLElement, nodeData?: Record<string, any>) {
      this.event = {
        name,
        timestamp: Date.now(),
        nodePosition: position,
        nodeDom,
        nodeData,
      };
    },
    setMold(val: number) {
      this.mold = val;
    },
  },
});

export default useMinderStore;
