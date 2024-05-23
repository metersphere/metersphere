<template>
  <a-spin :loading="loading" :tip="t('minder.loading')" class="ms-minder-editor-container">
    <div class="flex-1">
      <minderHeader
        :sequence-enable="props.sequenceEnable"
        :tag-enable="props.tagEnable"
        :progress-enable="props.progressEnable"
        :priority-count="props.priorityCount"
        :priority-prefix="props.priorityPrefix"
        :priority-start-with-zero="props.priorityStartWithZero"
        :tags="props.tags"
        :move-enable="props.moveEnable"
        :move-confirm="props.moveConfirm"
        :tag-edit-check="props.tagEditCheck"
        :tag-disable-check="props.tagDisableCheck"
        :priority-disable-check="props.priorityDisableCheck"
        :distinct-tags="props.distinctTags"
        :default-mold="props.defaultMold"
        :del-confirm="props.delConfirm"
        :arrange-enable="props.arrangeEnable"
        :mold-enable="props.moldEnable"
        :font-enable="props.fontEnable"
        :style-enable="props.styleEnable"
        :replaceable-tags="props.replaceableTags"
        :single-tag="props.singleTag"
        :insert-node="props.insertNode"
        :after-tag-edit="props.afterTagEdit"
        @mold-change="handleMoldChange"
      />
      <mainEditor
        :disabled="props.disabled"
        :sequence-enable="props.sequenceEnable"
        :tag-enable="props.tagEnable"
        :move-enable="props.moveEnable"
        :move-confirm="props.moveConfirm"
        :progress-enable="props.progressEnable"
        :import-json="props.importJson"
        :height="props.height"
        :tags="props.tags"
        :distinct-tags="props.distinctTags"
        :tag-edit-check="props.tagEditCheck"
        :tag-disable-check="props.tagDisableCheck"
        :priority-count="props.priorityCount"
        :priority-prefix="props.priorityPrefix"
        :priority-start-with-zero="props.priorityStartWithZero"
        :insert-node="props.insertNode"
        @after-mount="() => emit('afterMount')"
        @save="save"
        @enter-node="handleEnterNode"
      />
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
  import { debounce } from 'lodash-es';

  import MsTab from '@/components/pure/ms-tab/index.vue';
  import minderHeader from './main/header.vue';
  import mainEditor from './main/mainEditor.vue';

  import { useI18n } from '@/hooks/useI18n';

  import {
    delProps,
    editMenuProps,
    headerProps,
    insertProps,
    mainEditorProps,
    MinderJsonNode,
    moleProps,
    priorityProps,
    tagProps,
    viewMenuProps,
  } from './props';

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
    (e: 'save', data: Record<string, any>): void;
    (e: 'afterMount'): void;
    (e: 'enterNode', data: any): void;
    (e: 'nodeSelect', data: any): void;
  }>();

  const props = defineProps({
    ...headerProps,
    ...insertProps,
    ...editMenuProps,
    ...mainEditorProps,
    ...moleProps,
    ...priorityProps,
    ...tagProps,
    ...delProps,
    ...viewMenuProps,
  });

  const { t } = useI18n();

  const loading = defineModel<boolean>('loading', {
    default: false,
  });
  const activeExtraKey = defineModel<string>('activeExtraKey', {
    default: '',
  });
  const extraVisible = defineModel<boolean>('extraVisible', {
    default: false,
  });

  onMounted(async () => {
    window.minderProps = props;
  });

  function handleMoldChange(data: number) {
    emit('moldChange', data);
  }

  function save(data: Record<string, any>) {
    emit('save', data);
  }

  function handleEnterNode(data: any) {
    emit('enterNode', data);
  }

  onMounted(() => {
    nextTick(() => {
      if (window.minder.on) {
        window.minder.on(
          'selectionchange',
          debounce(() => {
            const selectedNode: MinderJsonNode = window.minder.getSelectedNode();
            if (Object.keys(window.minder).length > 0 && selectedNode) {
              emit('nodeSelect', selectedNode);
            }
          }, 300)
        );
      }
    });
  });
</script>

<style lang="less" scoped>
  .ms-minder-editor-container {
    @apply relative flex h-full w-full;
    .ms-minder-editor-extra {
      @apply flex flex-col overflow-hidden border-l;

      width: 0;
      border-color: var(--color-text-n8);
      transition: all 300ms ease-in-out;
      .ms-minder-editor-extra-content {
        @apply relative  flex-1 overflow-y-auto;
        .ms-scroll-bar();

        margin-top: 16px;
      }
    }
    .ms-minder-editor-extra--visible {
      width: 35%;
      min-width: 360px;
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
