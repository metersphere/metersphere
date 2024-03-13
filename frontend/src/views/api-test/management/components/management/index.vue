<template>
  <div class="flex gap-[8px] px-[16px] pt-[16px]">
    <a-select v-model:model-value="currentTab" class="w-[80px]" :options="tabOptions" />
    <MsEditableTab
      v-model:active-tab="activeApiTab"
      v-model:tabs="apiTabs"
      class="flex-1 overflow-hidden"
      @add="newTab"
    >
      <template #label="{ tab }">
        <apiMethodName
          v-if="tab.id !== 'all'"
          :method="tab.protocol === 'HTTP' ? tab.method : tab.protocol"
          class="mr-[4px]"
        />
        <a-tooltip :content="tab.name || tab.label" :mouse-enter-delay="500">
          <div class="one-line-text max-w-[144px]">
            {{ tab.name || tab.label }}
          </div>
        </a-tooltip>
      </template>
    </MsEditableTab>
    <a-select
      v-model:model-value="currentEnv"
      :options="envOptions"
      class="!w-[200px] pl-0 pr-[8px]"
      :loading="envLoading"
      allow-search
      @change="initEnvironment"
    >
      <template #prefix>
        <div class="flex cursor-pointer p-[8px]" @click.stop="goEnv">
          <icon-location class="text-[var(--color-text-4)]" />
        </div>
      </template>
    </a-select>
  </div>
  <api
    v-if="currentTab === 'api'"
    ref="apiRef"
    v-model:active-api-tab="activeApiTab"
    v-model:api-tabs="apiTabs"
    :active-module="props.activeModule"
    :offspring-ids="props.offspringIds"
    :protocol="props.protocol"
    :module-tree="props.moduleTree"
  />
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';

  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import api from './api/index.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  // import MockTable from '@/views/api-test/management/components/management/mock/mockTable.vue';
  import { getEnvironment, getEnvList } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';
  import router from '@/router';
  import useAppStore from '@/store/modules/app';

  import { ModuleTreeNode } from '@/models/common';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    protocol: string;
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const setActiveApi: ((params: RequestParam) => void) | undefined = inject('setActiveApi');

  const currentTab = ref('api');
  const tabOptions = [
    { label: 'API', value: 'api' },
    { label: 'CASE', value: 'case' },
  ];

  const apiRef = ref<InstanceType<typeof api>>();

  function newTab(apiInfo?: ModuleTreeNode | string) {
    if (apiInfo) {
      apiRef.value?.openApiTab(apiInfo);
    } else {
      apiRef.value?.addApiTab();
    }
  }

  const apiTabs = ref<RequestParam[]>([
    {
      id: 'all',
      label: t('apiTestManagement.allApi'),
      closable: false,
    } as RequestParam,
  ]);
  const activeApiTab = ref<RequestParam>(apiTabs.value[0] as RequestParam);

  watch(
    () => activeApiTab.value.id,
    () => {
      if (typeof setActiveApi === 'function') {
        setActiveApi(activeApiTab.value);
      }
    }
  );

  const currentEnv = ref('');
  const currentEnvConfig = ref<EnvConfig>();
  const envLoading = ref(false);
  const envOptions = ref<SelectOptionData[]>([]);

  async function initEnvironment() {
    try {
      currentEnvConfig.value = await getEnvironment(currentEnv.value);
      currentEnvConfig.value.id = currentEnv.value;
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

  function goEnv() {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_ENVIRONMENT_MANAGEMENT,
    });
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
  .ms-input-group--prepend();
  :deep(.arco-select-view-prefix) {
    margin-right: 8px;
    padding-right: 0;
    border-right: 1px solid var(--color-text-input-border);
  }
</style>
