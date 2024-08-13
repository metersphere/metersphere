import { debounce } from 'lodash-es';

import useMinderStore from '@/store/modules/components/minder-editor/index';
import type { MinderCustomEvent } from '@/store/modules/components/minder-editor/types';

import type { MinderEvent, MinderJsonNode } from '../props';

export interface UseEventListenerProps {
  handleDblclick?: () => void;
  handleContentChange?: (node?: MinderJsonNode) => void;
  handleSelectionChange?: (nodes: MinderJsonNode[]) => void;
  handleMinderEvent?: (event: MinderCustomEvent) => void;
  handleBeforeExecCommand?: (event: MinderEvent) => void;
  handleViewChange?: (event: MinderEvent) => void;
  handleDragStart?: (event: MinderEvent) => void;
  handleDragFinish?: (event: MinderEvent) => void;
}

export default function useEventListener(listener: UseEventListenerProps) {
  const { minder } = window;
  const minderStore = useMinderStore();

  // 是否正在拖拽节点中，拖拽时不触发节点选中事件，拖拽完成后才触发
  const isDragging = ref(false);
  // 拖拽触发时未处理的选中事件，拖拽完成后触发
  let selectionchangeEvent: (() => void) | undefined;

  // 双击编辑内容
  minder.on('dblclick', () => {
    if (listener.handleDblclick) {
      listener.handleDblclick();
    }
  });

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
      const nodes: MinderJsonNode[] = minder.getSelectedNodes();
      // 如果节点选中后即刻进行拖拽，则等待拖拽结束后再触发选中事件
      if (isDragging.value) {
        selectionchangeEvent = () => {
          if (listener.handleSelectionChange) {
            listener.handleSelectionChange(nodes);
          }
        };
      } else if (listener.handleSelectionChange) {
        listener.handleSelectionChange(nodes);
      }
    }, 300)
  );

  // 监听脑图执行命令前（可通过e.stopPropagation拦截命令执行）
  minder.on('beforeExecCommand', (e: MinderEvent) => {
    if (listener.handleBeforeExecCommand) {
      listener.handleBeforeExecCommand(e);
    }
  });

  // 监听脑图节点拖拽开始事件
  minder.on('dragStart', (e: MinderEvent) => {
    isDragging.value = true;
    if (listener.handleDragStart) {
      listener.handleDragStart(e);
    }
  });

  // 监听脑图节点拖拽结束事件
  minder.on('dragFinish', (e: MinderEvent) => {
    isDragging.value = false;
    if (listener.handleDragFinish) {
      listener.handleDragFinish(e);
    }
    if (selectionchangeEvent) {
      // 拖拽完成后触发未处理的选中事件
      selectionchangeEvent();
      selectionchangeEvent = undefined;
    }
  });

  // 监听脑图画布位移等视图变化
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
    () => minderStore.event.eventId,
    () => {
      if (listener.handleMinderEvent) {
        listener.handleMinderEvent(minderStore.event);
      }
    }
  );
}
