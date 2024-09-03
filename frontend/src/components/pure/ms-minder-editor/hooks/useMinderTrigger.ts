import useMinderStore from '@/store/modules/components/minder-editor';
import type { MinderCustomEvent, MinderNodePosition } from '@/store/modules/components/minder-editor/types';
import { sleep } from '@/utils';

import { MinderEventName } from '@/enums/minderEnum';

import type { MinderJsonNode } from '../props';
import { isNodeInMinderView } from '../script/tool/utils';

export default function useMinderTrigger(
  handleSelect?: (event: MinderCustomEvent, selectedNodes: MinderJsonNode[]) => void
) {
  const minderStore = useMinderStore();

  const triggerVisible = ref(false);
  const triggerOffset = ref([0, 0]);

  watch(
    () => minderStore.event.eventId,
    async () => {
      if (window.minder) {
        let nodePosition: MinderNodePosition | undefined;
        const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
        if (minderStore.event.name === MinderEventName.NODE_SELECT) {
          nodePosition = minderStore.event.nodePosition;
          if (handleSelect) {
            handleSelect(minderStore.event, selectedNodes);
          }
        }
        if (selectedNodes.length > 1) {
          // 多选时隐藏悬浮菜单
          triggerVisible.value = false;
          return;
        }
        if ([MinderEventName.VIEW_CHANGE, MinderEventName.DRAG_FINISH].includes(minderStore.event.name)) {
          // 脑图画布移动时，重新计算节点位置
          await sleep(300); // 拖拽完毕后会有 300ms 的动画，等待动画结束后再计算
          nodePosition = window.minder.getSelectedNode()?.getRenderBox();
        }
        const state = window.editor.fsm.state();
        if (
          nodePosition &&
          isNodeInMinderView(undefined, nodePosition, Math.min(nodePosition.width / 2, 200)) &&
          state !== 'input'
        ) {
          // 判断节点在脑图可视区域内且遮挡的节点不超过节点宽度的一半(超过 200px 则按 200px 算)且当前不是编辑名称状态，则显示菜单
          const nodeDomHeight = nodePosition.height || 0;
          triggerOffset.value = [nodePosition.x, nodePosition.y + nodeDomHeight + 4]; // trigger显示在节点下方4px处
          triggerVisible.value = true;
        } else {
          triggerVisible.value = false;
        }
      }
    },
    {
      immediate: true,
    }
  );

  return {
    triggerVisible,
    triggerOffset,
  };
}
