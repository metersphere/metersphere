<template>
  <MsCard class="mb-[16px]" :title="props.title" hide-back hide-footer auto-height no-content-padding>
    <a-tabs v-model:active-key="innerTab" class="no-content">
      <a-tab-pane v-for="item of tabList" :key="item.key" :title="item.title" />
    </a-tabs>
  </MsCard>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import MsCard from '@/components/pure/ms-card/index.vue';

  const props = defineProps<{
    activeTab: string;
    title?: string;
    tabList: { key: string; title: string }[];
  }>();
  const emit = defineEmits(['update:activeTab']);

  const innerTab = ref(props.activeTab);

  watch(
    () => props.activeTab,
    (val) => {
      innerTab.value = val;
    }
  );

  watch(
    () => innerTab.value,
    (val) => {
      emit('update:activeTab', val);
    }
  );
</script>

<style lang="less" scoped>
  :deep(.no-content) {
    .arco-tabs-content {
      display: none;
    }
  }
</style>
