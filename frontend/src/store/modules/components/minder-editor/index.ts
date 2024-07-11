import { defineStore } from 'pinia';

import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';

import useLocalForage from '@/hooks/useLocalForage';
import { getGenerateId, mapTree } from '@/utils';

import { MinderEventName, MinderKeyEnum } from '@/enums/minderEnum';

import { MinderNodePosition, MinderState, MinderStoreLocalItem, ModeType } from './types';

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
    activeMode: 'right',
    clipboard: [],
    minderUnsaved: false,
  }),
  getters: {
    getMinderUnsaved(): boolean {
      return this.minderUnsaved;
    },
    getMinderActiveMode(): ModeType {
      return this.activeMode;
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
    async getMode(MinderKey: MinderKeyEnum) {
      const { getItem } = useLocalForage();
      const minderStoreLocalMap = await getItem<MinderStoreLocalItem>(MinderKey);
      if (minderStoreLocalMap?.mode) {
        return minderStoreLocalMap.mode;
      }
      return 'right';
    },
    async setMode(MinderKey: MinderKeyEnum, mode: ModeType) {
      const { getItem, setItem } = useLocalForage();
      this.activeMode = mode;
      window.minder.execCommand('template', mode);
      try {
        const minderStoreLocalMap = await getItem<MinderStoreLocalItem>(MinderKey);
        if (minderStoreLocalMap) {
          minderStoreLocalMap.mode = mode;
          await setItem(MinderKey, minderStoreLocalMap);
        } else {
          await setItem(MinderKey, { mode });
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    },
    setClipboard(nodes?: MinderJsonNode[]) {
      this.clipboard = mapTree(nodes || [], (node) => {
        if (node.id !== 'fakeNode' && node.type !== 'tmp') {
          return { ...node, id: getGenerateId(), type: 'ADD' };
        }
        return null;
      });
    },
    setMinderUnsaved(val: boolean) {
      this.minderUnsaved = val;
    },
  },
});

export default useMinderStore;
