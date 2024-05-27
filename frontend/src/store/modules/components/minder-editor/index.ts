import { defineStore } from 'pinia';

import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';

import type { MinderEventName } from '@/enums/minderEnum';

import { MinderNodePosition, MinderState } from './types';

// 脑图组件的 store
const useMinderStore = defineStore('minder', {
  state: (): MinderState => ({
    event: {
      name: '' as MinderEventName,
      timestamp: 0,
      nodePosition: {
        x: 0,
        y: 0,
      },
      nodeDom: undefined,
      node: undefined,
    },
    mold: 0,
  }),
  actions: {
    /**
     * 脑图组件派发事件
     * @param name 事件名称
     * @param position 触发事件的节点/鼠标位置
     * @param nodeDom 节点 DOM
     * @param node 节点
     */
    dispatchEvent(name: MinderEventName, position?: MinderNodePosition, nodeDom?: HTMLElement, node?: MinderJsonNode) {
      this.event = {
        name,
        timestamp: Date.now(),
        nodePosition: position,
        nodeDom,
        node,
      };
    },
    setMold(val: number) {
      this.mold = val;
    },
  },
});

export default useMinderStore;
