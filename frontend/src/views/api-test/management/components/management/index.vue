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
    <a-tab-pane key="case" title="CASE" class="ms-api-tab-pane">
      <apiCase :active-module="props.activeModule" :offspring-ids="props.offspringIds" :protocol="protocol" />
    </a-tab-pane>
    <!-- <a-tab-pane key="mock" title="MOCK" class="ms-api-tab-pane">
      <mock-table
        ref="mockRef"
        :module-tree="props.moduleTree"
        :active-module="props.activeModule"
        :offspring-ids="props.offspringIds"
        :protocol="protocol"
      />
    </a-tab-pane> -->
    <!-- <a-tab-pane key="doc" title="API Docs" class="ms-api-tab-pane"> </a-tab-pane> -->
    <template #extra>
      <div class="flex items-center gap-[8px] pr-[24px]">
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]">
          <template #icon>
            <icon-location class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
        <MsSelect
          v-model:model-value="currentEnv"
          mode="static"
          :options="envOptions"
          class="!w-[150px]"
          :search-keys="['label']"
          :loading="envLoading"
          allow-search
          @change="initEnvironment"
        />
      </div>
    </template>
  </a-tabs>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';

  import MsSelect from '@/components/business/ms-select';
  import api from './api/index.vue';
  import apiCase from './case/index.vue';

  // import MockTable from '@/views/api-test/management/components/management/mock/mockTable.vue';
  import { getEnvironment, getEnvList } from '@/api/modules/api-test/common';
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
  const apiRef = ref<InstanceType<typeof api>>();

  function newTab(apiInfo?: ModuleTreeNode | string) {
    if (apiInfo) {
      apiRef.value?.openApiTab(apiInfo);
    } else {
      apiRef.value?.addApiTab();
    }
  }

  const currentEnv = ref('');
  const currentEnvConfig = ref({});
  const envLoading = ref(false);
  const envOptions = ref<SelectOptionData[]>([]);

  async function initEnvironment() {
    try {
      currentEnvConfig.value = await getEnvironment(currentEnv.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function initEnvList() {
    try {
      envLoading.value = true;
      const res = await getEnvList(appStore.currentProjectId);
      envOptions.value = res.map((item) => ({
        label: item.name,
        value: item.id,
      }));
      currentEnv.value = res[0]?.id || '';
      initEnvironment();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      envLoading.value = false;
    }
  }

  function refreshApiTable() {
    apiRef.value?.refreshTable();
  }

  onBeforeMount(() => {
    initEnvList();
  });

  /** 向孙组件提供属性 */
  provide('currentEnvConfig', readonly(currentEnvConfig));

  defineExpose({
    newTab,
    refreshApiTable,
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
