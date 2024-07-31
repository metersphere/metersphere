<template>
  <a-spin :loading="loading" class="ms-minder-editor-container">
    <div class="relative flex-1 overflow-hidden">
      <mainEditor
        v-model:import-json="importJson"
        v-bind="props"
        @after-mount="() => emit('afterMount')"
        @save="save"
        @float-menu-close="emit('floatMenuClose')"
      >
        <template #extractMenu>
          <slot name="extractMenu"></slot>
        </template>
      </mainEditor>
    </div>
    <div class="ms-minder-editor-extra" :class="[extraVisible ? 'ms-minder-editor-extra--visible' : '']">
      <div v-if="props.extractContentTabList?.length" class="pl-[16px] pt-[16px]">
        <MsTab v-model:activeKey="activeExtraKey" :content-tab-list="props.extractContentTabList" mode="button" />
      </div>
      <div class="ms-minder-editor-extra-content">
        <slot name="extractTabContent"></slot>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" name="minderEditor" setup>
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import mainEditor from './main/mainEditor.vue';

  import useMinderStore from '@/store/modules/components/minder-editor/index';
  import { MinderCustomEvent } from '@/store/modules/components/minder-editor/types';

  import { MinderEventName } from '@/enums/minderEnum';

  import useMinderEventListener from './hooks/useMinderEventListener';
  import useMinderOperation from './hooks/useMinderOperation';
  import useShortCut from './hooks/useShortCut';
  import {
    delProps,
    editMenuProps,
    floatMenuProps,
    headerProps,
    mainEditorProps,
    MinderEvent,
    MinderJson,
    MinderJsonNode,
    moleProps,
    priorityProps,
    tagProps,
    viewMenuProps,
  } from './props';
  import { isNodeInMinderView, setPriorityView } from './script/tool/utils';

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
    (e: 'save', data: MinderJson, callback: () => void): void;
    (e: 'afterMount'): void;
    (e: 'nodeSelect', data: MinderJsonNode): void;
    (e: 'contentChange', data?: MinderJsonNode): void;
    (e: 'action', event: MinderCustomEvent): void;
    (e: 'beforeExecCommand', event: MinderEvent): void;
    (e: 'nodeUnselect'): void;
    (e: 'floatMenuClose'): void;
  }>();

  const props = defineProps({
    ...headerProps,
    ...floatMenuProps,
    ...editMenuProps,
    ...mainEditorProps,
    ...moleProps,
    ...priorityProps,
    ...tagProps,
    ...delProps,
    ...viewMenuProps,
  });

  const minderStore = useMinderStore();

  const loading = defineModel<boolean>('loading', {
    default: false,
  });
  const activeExtraKey = defineModel<string>('activeExtraKey', {
    default: '',
  });
  const extraVisible = defineModel<boolean>('extraVisible', {
    default: false,
  });
  const importJson = defineModel<MinderJson>('importJson', {
    default: () => ({
      root: {},
      template: 'default',
      treePath: [] as any[],
    }),
  });

  watch(
    () => extraVisible.value,
    (val) => {
      const node: MinderJsonNode = window.minder.getSelectedNode();
      if (val && node) {
        const nodePosition = node?.getRenderBox();
        // 如果节点不在视图中，将节点移动到视图中
        if (nodePosition && !isNodeInMinderView(undefined, nodePosition, nodePosition.width / 2)) {
          setTimeout(() => {
            window.minder.execCommand('camera', node, 100);
          }, 300); // 抽屉动画 300ms
        }
      }
    }
  );

  function save(data: MinderJson, callback: () => void) {
    emit('save', data, callback);
  }

  const { appendChildNode, appendSiblingNode, minderDelete } = useMinderOperation({
    insertNode: props.insertNode,
    canShowMoreMenuNodeOperation: props.canShowMoreMenuNodeOperation,
    canShowPasteMenu: props.canShowPasteMenu,
  });
  const { unbindShortcuts } = useShortCut(
    {
      undo: () => {
        window.minderHistory?.undo();
      },
      redo: () => {
        window.minderHistory?.redo();
      },
      enter: () => {
        if (props.canShowEnterNode) {
          const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
          minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [selectedNodes[0]]);
        }
      },
      delete: () => {
        if (props.canShowMoreMenuNodeOperation && !props.disabled) {
          const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
          minderDelete(selectedNodes);
        }
      },
      expand: () => {
        const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
        if (selectedNodes.every((node) => node.isExpanded())) {
          // 选中的节点集合全部展开，则全部收起
          window.minder.execCommand('Collapse');
          minderStore.dispatchEvent(MinderEventName.COLLAPSE, undefined, undefined, undefined, selectedNodes);
        } else {
          // 选中的节点集合中有一个节点未展开，则全部展开
          window.minder.execCommand('Expand');
          if (!props.customPriority) {
            // 展开后，需要设置一次优先级展示，避免展开后优先级显示成脑图内置文案；如果设置了自定义优先级，则不在此设置，由外部自行处理
            setPriorityView(props.priorityStartWithZero, props.priorityPrefix);
          }
          minderStore.dispatchEvent(MinderEventName.EXPAND, undefined, undefined, undefined, selectedNodes);
        }
      },
      appendChildNode: () => {
        if (props.insertSonMenus.length > 0 || props.insertNode) {
          const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
          appendChildNode(selectedNodes);
        }
      },
      appendSiblingNode: () => {
        if (props.insertSiblingMenus.length > 0 || props.insertNode) {
          const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
          appendSiblingNode(selectedNodes);
        }
      },
    },
    {
      insertNode: props.insertNode,
      canShowMoreMenuNodeOperation: props.canShowMoreMenuNodeOperation,
      canShowPasteMenu: props.canShowPasteMenu,
    }
  );

  onMounted(() => {
    window.minderProps = props;
    useMinderEventListener({
      handleSelectionChange: (node?: MinderJsonNode) => {
        if (node) {
          emit('nodeSelect', node);
          const box = node.getRenderBox();
          minderStore.dispatchEvent(
            MinderEventName.NODE_SELECT,
            undefined,
            {
              ...box,
            },
            node.rc.node,
            [node]
          );
        } else {
          emit('nodeUnselect');
          minderStore.dispatchEvent(MinderEventName.NODE_UNSELECT);
        }
      },
      handleContentChange: (node?: MinderJsonNode) => {
        emit('contentChange', node);
      },
      handleMinderEvent: (event) => {
        emit('action', event);
      },
      handleBeforeExecCommand: (event) => {
        if (['movetoparent', 'arrange'].includes(event.commandName) && props.disabled) {
          event.stopPropagation();
          return;
        }
        emit('beforeExecCommand', event);
      },
      handleViewChange() {
        minderStore.dispatchEvent(MinderEventName.VIEW_CHANGE);
      },
      handleDragFinish() {
        minderStore.dispatchEvent(MinderEventName.DRAG_FINISH);
      },
    });
  });

  onUnmounted(() => {
    unbindShortcuts();
  });
</script>

<style lang="less" scoped>
  .ms-minder-editor-container {
    @apply relative flex h-full w-full;
    .ms-minder-editor-extra {
      @apply flex flex-col overflow-hidden  bg-white;

      width: 0;
      transition: all 300ms ease-in-out;
      :deep(.ms-tab--button-item) {
        flex: 1;
        text-align: center;
      }
      .ms-minder-editor-extra-content {
        @apply relative  flex-1 overflow-y-auto;
        .ms-scroll-bar();

        margin-top: 16px;
      }
    }
    .ms-minder-editor-extra--visible {
      @apply border-l;

      width: 35%;
      min-width: 360px;
      border-color: var(--color-text-n8);
      transition: all 300ms ease-in-out;
      animation: minWidth 300ms ease-in-out;
    }
    @keyframes minWidth {
      from {
        min-width: 0;
      }
      to {
        min-width: 360px;
      }
    }
  }
</style>
