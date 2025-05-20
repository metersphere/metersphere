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
        <template #batchMenu>
          <slot name="batchMenu"></slot>
        </template>
        <template #shortCutList>
          <slot name="shortCutList"></slot>
        </template>
      </mainEditor>
    </div>
    <div class="ms-minder-editor-extra" :class="[extraVisible ? 'ms-minder-editor-extra--visible' : '']">
      <div v-if="props.extractContentTabList?.length" class="pl-[16px] pt-[16px]">
        <MsTab
          v-model:active-key="activeExtraKey"
          :content-tab-list="props.extractContentTabList"
          mode="button"
          :disabled="props.disabledExtraTab"
        />
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
    batchMenuProps,
    delProps,
    dropdownMenuProps,
    editMenuProps,
    floatMenuProps,
    headerProps,
    mainEditorProps,
    MinderEvent,
    MinderJson,
    MinderJsonNode,
    moleProps,
    navigatorProps,
    priorityProps,
    tagProps,
    viewMenuProps,
  } from './props';
  import { isDisableNode, isNodeInMinderView } from './script/tool/utils';

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
    (e: 'save', data: MinderJson, callback: () => void): void;
    (e: 'afterMount'): void;
    (e: 'nodeSelect', data: MinderJsonNode): void;
    (e: 'nodeBatchSelect', data: MinderJsonNode[]): void;
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
    ...batchMenuProps,
    ...dropdownMenuProps,
    ...navigatorProps,
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

  const { appendChildNode, appendSiblingNode, minderDelete, minderExpand } = useMinderOperation(props);
  const { unbindShortcuts } = useShortCut(
    {
      input: () => {
        if (window.minder._status !== 'readonly' && !isDisableNode(window.minder)) {
          window.minderEditor.editText();
        }
      },
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
      save: (event: KeyboardEvent) => {
        event.preventDefault();
        minderStore.dispatchEvent(MinderEventName.SAVE_MINDER);
      },
      delete: () => {
        const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
        minderDelete(selectedNodes);
      },
      expand: () => {
        const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
        minderExpand(selectedNodes);
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
    props
  );

  onMounted(() => {
    window.minderProps = props;
    useMinderEventListener({
      handleSelectionChange: (nodes: MinderJsonNode[]) => {
        if (nodes && nodes.length > 1) {
          emit('nodeBatchSelect', nodes);
          return;
        }
        const node = nodes[0];
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
        // ！！！注意：这里如果是点击输入框以外的地方触发失焦，node 为 undefined，此时 node.data.changed 已标记 true
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
      @apply flex flex-col overflow-hidden;

      width: 0;
      background-color: var(--color-text-fff);
      transition: all 300ms ease-in-out;
      :deep(.ms-tab--button-item) {
        flex: 1;
        text-align: center;
      }
      .ms-minder-editor-extra-content {
        @apply relative  flex-1 overflow-auto;
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
