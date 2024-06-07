import { debounce } from 'lodash-es';

import useMinderStore from '@/store/modules/components/minder-editor/index';
import type { MinderCustomEvent } from '@/store/modules/components/minder-editor/types';

import type { MinderEvent, MinderJsonNode } from '../props';

export interface UseEventListenerProps {
  handleContentChange?: (node: MinderJsonNode) => void;
  handleSelectionChange?: (node: MinderJsonNode) => void;
  handleMinderEvent?: (event: MinderCustomEvent) => void;
  handleBeforeExecCommand?: (event: MinderEvent) => void;
  handleViewChange?: (event: MinderEvent) => void;
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

  // 监听脑图执行命令前（可通过e.stopPropagation拦截命令执行）
  minder.on('beforeExecCommand', (e: MinderEvent) => {
    if (listener.handleBeforeExecCommand) {
      listener.handleBeforeExecCommand(e);
    }
  });

  minder.on(
    'viewchange',
    debounce((e: MinderEvent) => {
      if (listener.handleViewChange) {
        listener.handleViewChange(e);
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
