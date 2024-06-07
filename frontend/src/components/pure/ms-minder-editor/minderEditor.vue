<template>
  <a-spin :loading="loading" class="ms-minder-editor-container">
    <div class="flex-1">
      <mainEditor v-model:import-json="importJson" v-bind="props" @after-mount="() => emit('afterMount')" @save="save">
        <template #extractMenu>
          <slot name="extractMenu"></slot>
        </template>
      </mainEditor>
    </div>
    <template v-if="props.extractContentTabList?.length">
      <div class="ms-minder-editor-extra" :class="[extraVisible ? 'ms-minder-editor-extra--visible' : '']">
        <div class="pl-[16px] pt-[16px]">
          <MsTab v-model:activeKey="activeExtraKey" :content-tab-list="props.extractContentTabList" mode="button" />
        </div>
        <div class="ms-minder-editor-extra-content">
          <slot name="extractTabContent"></slot>
        </div>
      </div>
    </template>
  </a-spin>
</template>

<script lang="ts" name="minderEditor" setup>
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import mainEditor from './main/mainEditor.vue';

  import useMinderStore from '@/store/modules/components/minder-editor/index';
  import { MinderCustomEvent } from '@/store/modules/components/minder-editor/types';

  import { MinderEventName } from '@/enums/minderEnum';

  import useMinderEventListener from './hooks/useMinderEventListener';
  import {
    delProps,
    editMenuProps,
    floatMenuProps,
    insertProps,
    mainEditorProps,
    MinderEvent,
    MinderJson,
    MinderJsonNode,
    moleProps,
    priorityProps,
    tagProps,
    viewMenuProps,
  } from './props';

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
    (e: 'save', data: MinderJson, callback: () => void): void;
    (e: 'afterMount'): void;
    (e: 'nodeSelect', data: MinderJsonNode): void;
    (e: 'contentChange', data: MinderJsonNode): void;
    (e: 'action', event: MinderCustomEvent): void;
    (e: 'beforeExecCommand', event: MinderEvent): void;
    (e: 'nodeUnselect'): void;
  }>();

  const props = defineProps({
    ...floatMenuProps,
    ...insertProps,
    ...editMenuProps,
    ...mainEditorProps,
    ...moleProps,
    ...priorityProps,
    ...tagProps,
    ...delProps,
    ...viewMenuProps,
  });

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

  const minderStore = useMinderStore();

  onMounted(async () => {
    window.minderProps = props;
  });

  function save(data: MinderJson, callback: () => void) {
    emit('save', data, callback);
  }

  onMounted(() => {
    useMinderEventListener({
      handleSelectionChange: () => {
        const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
        if (selectedNode) {
          emit('nodeSelect', selectedNode);
          const box = selectedNode.getRenderBox();
          minderStore.dispatchEvent(
            MinderEventName.NODE_SELECT,
            undefined,
            {
              ...box,
            },
            selectedNode.rc.node,
            [selectedNode]
          );
        } else {
          emit('nodeUnselect');
          minderStore.dispatchEvent(MinderEventName.NODE_UNSELECT);
        }
      },
      handleContentChange: (node: MinderJsonNode) => {
        emit('contentChange', node);
      },
      handleMinderEvent: (event) => {
        emit('action', event);
      },
      handleBeforeExecCommand: (event) => {
        emit('beforeExecCommand', event);
      },
      handleViewChange() {
        minderStore.dispatchEvent(MinderEventName.VIEW_CHANGE);
      },
    });
  });
</script>

<style lang="less" scoped>
  .ms-minder-editor-container {
    @apply relative flex h-full w-full;
    .ms-minder-editor-extra {
      @apply flex flex-col overflow-hidden;

      width: 0;
      transition: all 300ms ease-in-out;
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
