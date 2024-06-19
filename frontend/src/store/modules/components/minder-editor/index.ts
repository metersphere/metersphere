import { defineStore } from 'pinia';

import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';

import { getGenerateId, mapTree } from '@/utils';

import { MinderEventName } from '@/enums/minderEnum';

import { MinderNodePosition, MinderState } from './types';

// 脑图组件的 store
const useMinderStore = defineStore('minder', {
  state: (): MinderState => ({
    event: {
      name: '' as MinderEventName,
      eventId: '',
      params: '',
      nodePosition: {
        x: 0,
        y: 0,
      } as MinderNodePosition,
      nodeDom: undefined,
      nodes: undefined,
    },
    mold: 0,
    clipboard: [],
    minderUnsaved: false,
  }),
  getters: {
    getMinderUnsaved(): boolean {
      return this.minderUnsaved;
    },
  },
  actions: {
    /**
     * 脑图组件派发事件
     * @param name 事件名称
     * @param position 触发事件的节点/鼠标位置
     * @param nodeDom 节点 DOM
     * @param nodes 节点集合
     */
    dispatchEvent(
      name: MinderEventName,
      params?: string,
      position?: MinderNodePosition,
      nodeDom?: HTMLElement,
      nodes?: MinderJsonNode[]
    ) {
      this.event = {
        name,
        params,
        eventId: getGenerateId(),
        nodePosition: position,
        nodeDom,
        nodes,
      };
      if ([MinderEventName.COPY_NODE, MinderEventName.CUT_NODE].includes(name)) {
        this.setClipboard(nodes);
      }
    },
    setMold(val: number) {
      this.mold = val;
    },
    setClipboard(nodes?: MinderJsonNode[]) {
      this.clipboard = mapTree(nodes || [], (node) => ({ ...node, id: getGenerateId(), type: 'ADD' }));
    },
    setMinderUnsaved(val: boolean) {
      this.minderUnsaved = val;
    },
  },
});

export default useMinderStore;
