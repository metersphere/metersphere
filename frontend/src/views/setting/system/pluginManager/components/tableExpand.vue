<template>
  <a-scrollbar
    :style="{
      'overflow': 'auto',
      'min-height': '40px',
      'max-height': '120px',
    }"
  >
    <div class="ms-scroll">
      <div v-for="(item, index) in record.pluginForms" :key="item.id" class="ms-self"
        ><span class="circle"> {{ index + 1 }} </span
        ><span class="cursor-pointer text-[rgb(var(--primary-6))]" @click="emit('MessageEvent', record, item)">{{
          item.name
        }}</span></div
      >
    </div>
  </a-scrollbar>
</template>

<script setup lang="ts">
  import type { PluginForms, PluginItem } from '@/models/setting/plugin';

  defineProps<{
    record: PluginItem;
  }>();
  const emit = defineEmits<{
    (e: 'MessageEvent', record: PluginItem, item: PluginForms): void;
  }>();
</script>

<style scoped lang="less">
  :deep(.arco-scrollbar-container + .arco-scrollbar-track-direction-vertical) {
    left: 0 !important;
  }
  .ms-scroll {
    width: 100%;
  }
  .circle {
    color: var(--color-text-3);
    background: var(--color-fill-3);
    @apply ml-6 mr-10 inline-block h-4 w-4 text-center text-xs leading-4;
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 0 !important;
  }
  .ms-self {
    height: 40px;
    line-height: 40px;
    border-bottom: 1px solid var(--color-text-n8);
    @apply flex items-center align-middle leading-6;
  }
</style>
