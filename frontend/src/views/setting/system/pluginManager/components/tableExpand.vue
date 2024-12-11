<template>
  <div class="w-full">
    <div v-for="(item, index) in recordItem.pluginForms" :key="item.id" class="ms-self">
      <span class="circle text-[12px] leading-[16px]"> {{ index + 1 }} </span>
      <span class="cursor-pointer text-[rgb(var(--primary-5))]" @click="getScriptEmit(recordItem, item)">
        {{ item.name }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { PluginForms, PluginItem } from '@/models/setting/plugin';

  defineProps<{
    recordItem: PluginItem;
  }>();
  const emit = defineEmits<{
    (e: 'messageEvent', record: PluginItem, item: PluginForms): void;
  }>();

  const originPluginId = ref('');
  const getScriptEmit = (record: PluginItem, item: PluginForms) => {
    if (originPluginId.value !== item.id) {
      emit('messageEvent', record, item);
      originPluginId.value = item.id;
    }
  };
</script>

<style scoped lang="less">
  :deep(.arco-scrollbar-container + .arco-scrollbar-track-direction-vertical) {
    left: 0 !important;
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
    &:last-of-type {
      border-bottom: none;
    }
  }
  .ms-self:hover {
    background: var(--color-fill-1);
  }
</style>
