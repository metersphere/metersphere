<template>
  <MsCard class="mb-[16px]" :title="props.title" hide-back hide-footer auto-height no-content-padding no-bottom-radius>
    <a-tabs v-model:active-key="innerTab" class="no-content">
      <a-tab-pane v-for="item of permissionTabList" :key="item.key" :title="item.title" />
    </a-tabs>
  </MsCard>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';

  import { hasAnyPermission } from '@/utils/permission';

  const props = defineProps<{
    activeTab: string;
    title?: string;
    tabList: { key: string; title: string; permission?: string[] }[];
  }>();
  const emit = defineEmits(['update:activeTab']);

  const innerTab = ref(props.activeTab);

  const permissionTabList = computed(() => {
    return props.tabList.filter((item: any) => hasAnyPermission(item.permission));
  });

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
