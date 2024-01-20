<template>
  <div class="main-container">
    <header-menu
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
      @mold-change="handleMoldChange"
    />
    <main-editor
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
      @after-mount="emit('afterMount')"
      @save="save"
      @enter-node="handleEnterNode"
    />
  </div>
</template>

<script lang="ts" name="minderEditor" setup>
  import { onMounted } from 'vue';

  import headerMenu from './main/header.vue';
  import mainEditor from './main/mainEditor.vue';

  import { delProps, editMenuProps, mainEditorProps, moleProps, priorityProps, tagProps, viewMenuProps } from './props';

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
    (e: 'save', data: Record<string, any>): void;
    (e: 'afterMount'): void;
    (e: 'enterNode', data: any): void;
    (e: 'nodeClick', data: any): void;
  }>();

  const props = defineProps({
    ...editMenuProps,
    ...mainEditorProps,
    ...moleProps,
    ...priorityProps,
    ...tagProps,
    ...delProps,
    ...viewMenuProps,
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
        window.minder.on('mousedown', (e: any) => {
          if (e.originEvent.button === 0) {
            // 鼠标左键点击
            const selectedNode = window.minder.getSelectedNode();
            if (Object.keys(window.minder).length > 0 && selectedNode) {
              emit('nodeClick', selectedNode.data);
            }
          }
        });
      }
    });
  });
</script>
