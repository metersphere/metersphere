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
    <a-tab-pane key="case" title="CASE" class="ms-api-tab-pane"> </a-tab-pane>
    <a-tab-pane key="mock" title="MOCK" class="ms-api-tab-pane"> </a-tab-pane>
    <!-- <a-tab-pane key="doc" title="API Docs" class="ms-api-tab-pane"> </a-tab-pane> -->
    <template #extra>
      <div class="flex items-center gap-[8px] pr-[24px]">
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]">
          <template #icon>
            <icon-location class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
        <MsSelect
          v-model:model-value="checkedEnv"
          mode="static"
          :options="envOptions"
          class="!w-[150px]"
          :search-keys="['label']"
          allow-search
        />
      </div>
    </template>
  </a-tabs>
</template>

<script setup lang="ts">
  import MsSelect from '@/components/business/ms-select';
  import api from './api/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
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

  const checkedEnv = ref('DEV');
  const envOptions = ref([
    {
      label: 'DEV',
      value: 'DEV',
    },
    {
      label: 'TEST',
      value: 'TEST',
    },
    {
      label: 'PRE',
      value: 'PRE',
    },
    {
      label: 'PROD',
      value: 'PROD',
    },
  ]);

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
