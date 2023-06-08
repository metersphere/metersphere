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
    />
  </div>
</template>

<script lang="ts" name="minderEditor" setup>
  import { onMounted } from 'vue';
  import headerMenu from './main/header.vue';
  import mainEditor from './main/mainEditor.vue';
  import { editMenuProps, mainEditorProps, moleProps, priorityProps, tagProps, delProps, viewMenuProps } from './props';

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
    (e: 'save', data: Record<string, any>): void;
    (e: 'afterMount'): void;
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
</script>
