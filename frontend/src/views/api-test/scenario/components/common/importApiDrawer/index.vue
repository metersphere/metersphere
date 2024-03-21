<template>
  <MsDrawer
    v-model:visible="visible"
    :title="t('apiScenario.importSystemApi')"
    :width="1200"
    no-content-padding
    disabled-width-drag
  >
    <div class="h-full w-full overflow-hidden">
      <a-tabs v-model:active-key="activeKey" @change="resetModule">
        <a-tab-pane key="api" :title="t('apiScenario.api')" />
        <a-tab-pane key="case" :title="t('apiScenario.case')" />
        <a-tab-pane key="scenario" :title="t('apiScenario.scenario')" />
      </a-tabs>
      <a-divider :margin="0"></a-divider>
      <div class="flex h-[calc(100%-49px)]">
        <div class="w-[300px] border-r p-[16px]">
          <div class="flex flex-col">
            <div class="mb-[12px] flex items-center gap-[8px]">
              <MsProjectSelect v-model:project="currentProject" @change="resetModule" />
              <a-select
                v-model:model-value="protocol"
                :options="protocolOptions"
                class="w-[90px]"
                @change="resetModule"
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
            :selected-apis="selectedApis"
            :selected-cases="selectedCases"
            :selected-scenarios="selectedScenarios"
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
          <a-button type="primary" :disabled="totalSelected === 0" @click="handleCopy">
            {{ t('common.copy') }}
          </a-button>
          <a-button type="primary" :disabled="totalSelected === 0" @click="handleQuote">
            {{ t('common.quote') }}
          </a-button>
        </div>
      </div>
    </template>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import { MsTableDataItem } from '@/components/pure/ms-table/type';
  import MsProjectSelect from '@/components/business/ms-project-select/index.vue';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import moduleTree from './moduleTree.vue';
  import apiTable from './table.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
  import { ApiScenarioTableItem } from '@/models/apiTest/scenario';

  export interface ImportData {
    api: MsTableDataItem<ApiDefinitionDetail>[];
    case: MsTableDataItem<ApiCaseDetail>[];
    scenario: MsTableDataItem<ApiScenarioTableItem>[];
  }

  const emit = defineEmits<{
    (e: 'copy', data: ImportData): void;
    (e: 'quote', data: ImportData): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });
  const activeKey = ref<'api' | 'case' | 'scenario'>('api');

  const selectedApis = ref<MsTableDataItem<ApiDefinitionDetail>[]>([]);
  const selectedCases = ref<MsTableDataItem<ApiCaseDetail>[]>([]);
  const selectedScenarios = ref<MsTableDataItem<ApiScenarioTableItem>[]>([]);
  const totalSelected = computed(() => {
    return selectedApis.value.length + selectedCases.value.length + selectedScenarios.value.length;
  });

  function handleTableSelect(data: MsTableDataItem<ApiCaseDetail | ApiDefinitionDetail | ApiScenarioTableItem>[]) {
    if (activeKey.value === 'api') {
      selectedApis.value = data as MsTableDataItem<ApiDefinitionDetail>[];
    } else if (activeKey.value === 'case') {
      selectedCases.value = data as MsTableDataItem<ApiCaseDetail>[];
    } else if (activeKey.value === 'scenario') {
      selectedScenarios.value = data as MsTableDataItem<ApiScenarioTableItem>[];
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

  function resetModule() {
    moduleTreeRef.value?.init(activeKey.value);
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
    emit(
      'copy',
      cloneDeep({
        api: selectedApis.value,
        case: selectedCases.value,
        scenario: selectedScenarios.value,
      })
    );
    handleCancel();
  }

  function handleQuote() {
    emit(
      'quote',
      cloneDeep({
        api: selectedApis.value,
        case: selectedCases.value,
        scenario: selectedScenarios.value,
      })
    );
    handleCancel();
  }

  onBeforeMount(() => {
    initProtocolList();
  });

  // 外面需要使用 v-if 动态渲染
  onMounted(() => {
    nextTick(() => {
      // 外面使用 v-if 动态渲染时，需要在 nextTick 中执行初始化数据，因为子组件 ref 引用需要在渲染后才能获取到
      moduleTreeRef.value?.init(activeKey.value);
    });
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
