<template>
  <MsDrawer
    v-model:visible="visible"
    :title="t('apiScenario.importSystemApi')"
    :width="1200"
    no-content-padding
    disabled-width-drag
  >
    <div class="h-full w-full overflow-hidden">
      <a-tabs v-model:active-key="activeKey" @change="resetModuleAndTable">
        <a-tab-pane key="api" :title="t('apiScenario.api')" />
        <a-tab-pane key="case" :title="t('apiScenario.case')" />
        <a-tab-pane key="scenario" :title="t('apiScenario.scenario')" />
      </a-tabs>
      <a-divider :margin="0"></a-divider>
      <div class="flex">
        <div class="w-[300px] border-r p-[16px]">
          <div class="flex flex-col">
            <div class="mb-[12px] flex items-center gap-[8px]">
              <MsProjectSelect v-model:project="currentProject" @change="resetModuleAndTable" />
              <a-select
                v-model:model-value="protocol"
                :options="protocolOptions"
                class="w-[90px]"
                @change="resetModuleAndTable"
              />
            </div>
            <moduleTree
              ref="moduleTreeRef"
              :type="activeKey"
              :project-id="currentProject"
              :protocol="protocol"
              @select="handleModuleSelect"
            />
          </div>
        </div>
        <div class="table-container">
          <apiTable
            ref="apiTableRef"
            :module="activeModule"
            :type="activeKey"
            :protocol="protocol"
            :project-id="currentProject"
            :module-ids="moduleIds"
            @select="handleTableSelect"
          />
        </div>
      </div>
    </div>
    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-[4px]">
          <div class="second-text">{{ t('apiScenario.sumSelected') }}</div>
          <div class="main-text">{{ totalSelected }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.api') }}</div>
          <div class="main-text">{{ selectedApis.length }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.case') }}</div>
          <div class="main-text">{{ selectedCases.length }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.scenario') }}</div>
          <div class="main-text">{{ selectedScenarios.length }}</div>
          <a-divider v-show="totalSelected > 0" direction="vertical" :margin="4"></a-divider>
          <MsButton v-show="totalSelected > 0" type="text" class="!mr-0 ml-[4px]" @click="clearAll">
            {{ t('common.clear') }}
          </MsButton>
        </div>
        <div class="flex items-center gap-[12px]">
          <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
          <a-button type="primary" @click="handleCopy">{{ t('common.copy') }}</a-button>
          <a-button type="primary" @click="handleQuote">{{ t('common.quote') }}</a-button>
        </div>
      </div>
    </template>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsProjectSelect from '@/components/business/ms-project-select/index.vue';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import moduleTree from './moduleTree.vue';
  import apiTable from './table.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const emit = defineEmits<{
    (e: 'copy', data: any[]): void;
    (e: 'quote', data: any[]): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });
  const activeKey = ref<'api' | 'case' | 'scenario'>('api');

  const selectedApis = ref<any[]>([]);
  const selectedCases = ref<any[]>([]);
  const selectedScenarios = ref<any[]>([]);
  const totalSelected = computed(() => {
    return selectedApis.value.length + selectedCases.value.length + selectedScenarios.value.length;
  });

  function handleTableSelect(ids: (string | number)[]) {
    if (activeKey.value === 'api') {
      selectedApis.value = ids;
    } else if (activeKey.value === 'case') {
      selectedCases.value = ids;
    } else if (activeKey.value === 'scenario') {
      selectedScenarios.value = ids;
    }
  }

  const activeModule = ref<MsTreeNodeData>({});
  const currentProject = ref(appStore.currentProjectId);
  const protocol = ref('HTTP');
  const protocolOptions = ref<SelectOptionData[]>([]);
  const protocolLoading = ref(false);

  async function initProtocolList() {
    try {
      protocolLoading.value = true;
      const res = await getProtocolList(appStore.currentOrgId);
      protocolOptions.value = res.map((e) => ({
        label: e.protocol,
        value: e.protocol,
        polymorphicName: e.polymorphicName,
        pluginId: e.pluginId,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      protocolLoading.value = false;
    }
  }

  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const apiTableRef = ref<InstanceType<typeof apiTable>>();
  const moduleIds = ref<(string | number)[]>([]);

  function resetModuleAndTable() {
    moduleTreeRef.value?.init(activeKey.value);
    apiTableRef.value?.loadPage(['root']); // 这里传入根模块id，因为模块需要加载，且默认选中的就是默认模块
  }

  function handleModuleSelect(ids: (string | number)[], node: MsTreeNodeData) {
    activeModule.value = node;
    moduleIds.value = ids;
    apiTableRef.value?.loadPage(ids);
  }

  function clearAll() {
    selectedApis.value = [];
    selectedCases.value = [];
    selectedScenarios.value = [];
  }

  function handleCancel() {
    clearAll();
    visible.value = false;
  }

  function handleCopy() {
    emit('copy', [...selectedApis.value, ...selectedCases.value, ...selectedScenarios.value]);
    handleCancel();
  }

  function handleQuote() {
    emit('quote', [...selectedApis.value, ...selectedCases.value, ...selectedScenarios.value]);
    handleCancel();
  }

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        resetModuleAndTable();
      }
    },
    {
      immediate: true,
    }
  );

  onBeforeMount(() => {
    initProtocolList();
  });
</script>

<style lang="less" scoped>
  .second-text {
    color: var(--color-text-2);
  }
  .main-text {
    color: rgb(var(--primary-5));
  }
  :deep(.arco-tabs-content) {
    @apply hidden;
  }
  .table-container {
    @apply overflow-auto;
    .ms-scroll-bar();

    padding: 16px;
    width: calc(100% - 300px);
  }
</style>
