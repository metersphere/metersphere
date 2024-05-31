import { debounce } from 'lodash-es';

import useMinderStore from '@/store/modules/components/minder-editor/index';
import type { MinderEvent } from '@/store/modules/components/minder-editor/types';

import type { MinderJsonNode } from '../props';

export interface UseEventListenerProps {
  handleContentChange?: (node: MinderJsonNode) => void;
  handleSelectionChange?: (node: MinderJsonNode) => void;
  handleMinderEvent?: (event: MinderEvent) => void;
}

export default function useEventListener(listener: UseEventListenerProps) {
  const { minder } = window;
  const minderStore = useMinderStore();

  // 监听脑图节点内容变化
  minder.on('contentchange', () => {
    const node: MinderJsonNode = minder.getSelectedNode();
    if (listener.handleContentChange) {
      listener.handleContentChange(node);
    }
  });

  // 监听脑图选中节点变化
  minder.on(
    'selectionchange',
    debounce(() => {
      const node: MinderJsonNode = minder.getSelectedNode();
      if (listener.handleSelectionChange) {
        listener.handleSelectionChange(node);
      }
    }, 300)
  );

  // 监听脑图自定义事件
  watch(
    () => minderStore.event.timestamp,
    () => {
      if (listener.handleMinderEvent) {
        listener.handleMinderEvent(minderStore.event);
      }
    }
  );
}
