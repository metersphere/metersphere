<template>
  <a-tabs v-model:active-key="activeTab" animation lazy-load class="ms-api-tab-nav">
    <a-tab-pane key="api" title="API" class="ms-api-tab-pane">
      <api
        ref="apiRef"
        :module-tree="props.moduleTree"
        :active-module="props.activeModule"
        :offspring-ids="props.offspringIds"
        :protocol="protocol"
      />
    </a-tab-pane>
    <a-tab-pane key="case" title="CASE" class="ms-api-tab-pane"></a-tab-pane>
  </a-tabs>
</template>

<script setup lang="ts">
  import api from './api/apiTable.vue';

  import useAppStore from '@/store/modules/app';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    protocol: string;
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();

  const appStore = useAppStore();

  const activeTab = ref('api');
</script>

<style lang="less" scoped>
  .ms-api-tab-nav {
    @apply h-full;

    :deep(.arco-tabs-content) {
      height: calc(100% - 51px);

      .arco-tabs-content-list {
        @apply h-full;

        .arco-tabs-pane {
          @apply h-full;
        }
      }
    }

    :deep(.arco-tabs-nav) {
      border-bottom: 1px solid var(--color-text-n8);
    }
  }
</style>
