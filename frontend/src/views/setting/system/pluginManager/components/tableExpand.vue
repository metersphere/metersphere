<template>
  <a-scrollbar
    :style="{
      'overflow': 'auto',
      'min-height': '54px',
      'max-height': '270px',
    }"
  >
    <div class="ms-scroll">
      <div v-for="(item, index) in record.pluginForms" :key="item.id" class="ms-self"
        ><span class="circle text-xs leading-[16px]"> {{ index + 1 }} </span
        ><span class="cursor-pointer text-[rgb(var(--primary-5))]" @click="emit('MessageEvent', record, item)">{{
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
    width: 16px;
    height: 16px;
    color: var(--color-text-3);
    background: var(--color-fill-3);
    @apply ml-6 mr-10 text-center;
  }
  :deep(.arco-table-tr-expand .arco-table-cell) {
    padding: 0 !important;
  }
  .ms-self {
    height: 54px;
    line-height: 54px;
    border-bottom: 1px solid var(--color-text-n8);
    @apply flex items-center align-middle leading-6;
  }
  .ms-self:hover {
    background: var(--color-fill-1);
  }
</style>
