<template>
  <a-tabs v-model:active-key="activeTab" animation lazy-load class="ms-api-tab-nav">
    <a-tab-pane key="api" title="API" class="ms-api-tab-pane">
      <api
        ref="apiRef"
        :module-tree="props.moduleTree"
        :active-module="props.activeModule"
        :all-count="props.allCount"
        :offspring-ids="props.offspringIds"
        :protocol="protocol"
      />
    </a-tab-pane>
    <a-tab-pane key="case" title="CASE" class="ms-api-tab-pane"> </a-tab-pane>
    <a-tab-pane key="mock" title="MOCK" class="ms-api-tab-pane"> </a-tab-pane>
    <a-tab-pane key="doc" :title="t('apiTestManagement.doc')" class="ms-api-tab-pane"> </a-tab-pane>
  </a-tabs>
</template>

<script setup lang="ts">
  import api from './api/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    allCount: number;
    activeModule: string;
    offspringIds: string[];
    protocol: string;
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();

  const { t } = useI18n();

  const activeTab = ref('api');
  const apiRef = ref<InstanceType<typeof api>>();

  function newTab(apiInfo?: ModuleTreeNode) {
    if (apiInfo) {
      apiRef.value?.openApiTab(apiInfo);
    } else {
      apiRef.value?.addApiTab();
    }
  }

  defineExpose({
    newTab,
  });
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
