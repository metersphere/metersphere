<template>
  <MsDrawer
    v-model:visible="visible"
    :title="props.singleSelect ? t('common.replace') : t('apiScenario.importSystemApi')"
    :width="1200"
    no-content-padding
    disabled-width-drag
  >
    <div class="h-full w-full overflow-hidden">
      <a-tabs v-model:active-key="activeKey">
        <a-tab-pane key="api" :title="t('apiScenario.api')" />
        <a-tab-pane key="case" :title="t('apiScenario.case')" />
        <a-tab-pane key="scenario" :title="t('apiScenario.scenario')" />
      </a-tabs>
      <a-divider :margin="0"></a-divider>
      <div class="flex h-[calc(100%-49px)]">
        <div v-show="!isAdvancedSearchMode" class="w-[300px] border-r border-[var(--color-text-n8)] p-[16px]">
          <div class="flex flex-col">
            <div class="mb-[12px] flex items-center gap-[8px] overflow-hidden">
              <MsProjectSelect
                v-model:project="currentProject"
                class="flex-1 overflow-hidden"
                @change="handleChangeProject"
              />
              <a-select
                v-if="activeKey !== 'scenario'"
                v-model:model-value="protocol"
                class="w-[90px]"
                @change="clearSelected()"
              >
                <a-tooltip
                  v-for="item of protocolOptions"
                  :key="item.value as string"
                  :content="item.label"
                  :mouse-enter-delay="300"
                >
                  <a-option :value="item.value">
                    {{ item.label }}
                  </a-option>
                </a-tooltip>
              </a-select>
            </div>
            <moduleTree
              ref="moduleTreeRef"
              v-model:checkedKeys="currentUseTreeSelection.checkedKeys"
              v-model:halfCheckedKeys="currentUseTreeSelection.halfCheckedKeys"
              :type="activeKey"
              :project-id="currentProject"
              :single-select="props.singleSelect"
              @select-parent="currentUseTreeSelection.selectParent"
              @check="currentUseTreeSelection.checkNode"
              @select="handleModuleSelect"
              @init="initModuleTreeData"
            >
              <a-checkbox
                v-if="!props.singleSelect"
                v-model:model-value="currentUseTreeSelection.isCheckedAll"
                class="mr-[8px]"
                :indeterminate="currentUseTreeSelection.indeterminate"
                @change="currentUseTreeSelection.checkAllModule"
                @click.stop
              />
            </moduleTree>
          </div>
        </div>
        <div :class="[`table-container ${!isAdvancedSearchMode ? 'w-[calc(100%-300px)]' : 'w-full'}`]">
          <apiTable
            ref="apiTableRef"
            v-model:apiSelectedModulesMaps="apiUseTreeSelection.selectedModulesMaps.value"
            v-model:caseSelectedModulesMaps="caseUseTreeSelection.selectedModulesMaps.value"
            v-model:scenarioSelectedModulesMaps="scenarioUseTreeSelection.selectedModulesMaps.value"
            :module="activeModule"
            :type="activeKey"
            :protocol="protocol"
            :protocol-options="protocolOptions"
            :protocols-param="protocolsParam"
            :project-id="currentProject"
            :module-ids="moduleIds"
            :scenario-id="props.scenarioId"
            :case-id="props.caseId"
            :api-id="props.apiId"
            :single-select="props.singleSelect"
            :search-placeholder="
              activeKey === 'scenario' ? t('apiScenario.quoteTableSearchTip2') : t('apiScenario.quoteTableSearchTip')
            "
            :module-tree="folderTree"
            :modules-count="modulesCount"
            @handle-adv-search="handleAdvSearch"
          />
        </div>
      </div>
    </div>
    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-[4px]">
          <div class="second-text">{{ t('apiScenario.sumSelected') }}</div>
          <div class="main-text">{{ totalCount }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.api') }}</div>
          <div class="main-text">{{ apiTotal }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.case') }}</div>
          <div class="main-text">{{ caseTotal }}</div>
          <a-divider direction="vertical" :margin="4"></a-divider>
          <div class="second-text">{{ t('apiScenario.scenario') }}</div>
          <div class="main-text">{{ scenarioTotal }}</div>
          <a-divider v-show="totalCount > 0" direction="vertical" :margin="4"></a-divider>
          <MsButton
            v-show="totalCount && !props.singleSelect"
            type="text"
            class="!mr-0 ml-[4px]"
            @click="clearSelected"
          >
            {{ t('common.clear') }}
          </MsButton>
        </div>
        <div class="flex items-center gap-[12px]">
          <a-button type="secondary" :disabled="loading" @click="handleCancel">{{ t('common.cancel') }}</a-button>
          <a-button
            type="primary"
            :loading="loading"
            :disabled="totalCount === 0"
            @click="handleCopyOrQuote(ScenarioStepRefType.COPY)"
          >
            {{ t('common.copy') }}
          </a-button>
          <a-button
            type="primary"
            :loading="loading"
            :disabled="totalCount === 0"
            @click="handleCopyOrQuote(ScenarioStepRefType.REF)"
          >
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
  import type { moduleKeysType, saveParams } from '@/components/business/ms-associate-case/types';
  import useTreeSelection from '@/components/business/ms-associate-case/useTreeSelection';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import moduleTree from './moduleTree.vue';
  import MsProjectSelect from './projectSelect.vue';
  import apiTable from './table.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import { scenarioAssociateExport } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId, mapTree } from '@/utils';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';

  import type { ApiCaseDetail, ApiDefinitionDetail } from '@/models/apiTest/management';
  import type { ApiScenarioTableItem, ImportSystemData } from '@/models/apiTest/scenario';
  import type { ModuleTreeNode } from '@/models/common';
  import { ProtocolKeyEnum, ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

  export interface ImportData {
    api: MsTableDataItem<ApiDefinitionDetail>[];
    case: MsTableDataItem<ApiCaseDetail>[];
    scenario: MsTableDataItem<ApiScenarioTableItem>[];
  }

  const props = defineProps<{
    scenarioId?: string | number;
    caseId?: string | number;
    apiId?: string | number;
    singleSelect?: boolean;
  }>();
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

  const loading = ref(false);

  const activeModule = ref<MsTreeNodeData>({});
  const currentProject = ref(appStore.currentProjectId);
  const protocol = ref('');
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

  const isAdvancedSearchMode = ref(false);
  const protocolsParam = computed(() => {
    const allProtocolList = protocolOptions.value.map((item) => item.value as string);
    return !isAdvancedSearchMode.value ? [protocol.value] : allProtocolList;
  });

  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const apiTableRef = ref<InstanceType<typeof apiTable>>();
  const moduleIds = ref<(string | number)[]>([]);

  function initModuleTree() {
    nextTick(() => {
      moduleTreeRef.value?.init(protocolsParam.value, activeKey.value);
    });
  }

  watch(
    () => protocol.value,
    (val) => {
      setLocalStorage(ProtocolKeyEnum.API_SCENARIO_IMPORT_PROTOCOL, val);
      initModuleTree();
    }
  );

  function handleAdvSearch(isStartAdvance: boolean) {
    isAdvancedSearchMode.value = isStartAdvance;
    initModuleTree();
  }

  function handleModuleSelect(ids: (string | number)[], node: MsTreeNodeData) {
    activeModule.value = node;
    moduleIds.value = ids;
    if (!isAdvancedSearchMode.value) {
      apiTableRef.value?.loadPage(ids);
    }
  }
  function handleCancel() {
    visible.value = false;
  }

  /**
   * 获取复制或引用的步骤数据
   * @param refType 复制或引用
   */
  function getScenarioSteps(
    refType: ScenarioStepRefType.COPY | ScenarioStepRefType.REF,
    scenarioData: MsTableDataItem<ApiScenarioTableItem>[]
  ) {
    let fullScenarioArr: MsTableDataItem<ApiScenarioTableItem>[] = [];
    if (refType === ScenarioStepRefType.COPY) {
      // 复制需要递归给每个节点生成新的uniqueId，并记录copyFromStepId
      fullScenarioArr = mapTree<MsTableDataItem<ApiScenarioTableItem>>(scenarioData, (node) => {
        const id = getGenerateId();
        if (
          node.parent &&
          node.parent.stepType === ScenarioStepType.API_SCENARIO &&
          [ScenarioStepRefType.REF, ScenarioStepRefType.PARTIAL_REF].includes(node.parent.refType)
        ) {
          // 如果根节点是引用场景
          node.isQuoteScenarioStep = true; // 标记为引用场景下的子步骤
          node.isRefScenarioStep = node.parent.refType === ScenarioStepRefType.REF; // 标记为完全引用场景
          node.draggable = false; // 引用场景下的任何步骤不可拖拽
        } else if (node.parent) {
          // 如果有父节点
          node.isQuoteScenarioStep = node.parent.isQuoteScenarioStep; // 复用父节点的引用场景标记
          node.isRefScenarioStep = node.parent.isRefScenarioStep; // 复用父节点的是否完全引用场景标记
          node.draggable = !node.parent.isQuoteScenarioStep; // 引用场景下的任何步骤不可拖拽
        }
        return {
          ...node,
          copyFromStepId: node.id,
          originProjectId: node.projectId,
          id: node.isQuoteScenarioStep ? node.id : id, // 引用场景下的步骤 id 不变，其他情况的步骤生成新的 id
          uniqueId: id,
          isNew: true,
        };
      });
    } else {
      // 引用只需要给场景节点生成新的步骤 id，内部步骤只需要生成uniqueId作为前端渲染 id
      fullScenarioArr = scenarioData.map((e) => {
        const id = getGenerateId();
        return {
          ...e,
          children: mapTree<MsTableDataItem<ApiScenarioTableItem>>(e.children || [], (node) => {
            const childId = getGenerateId();
            return {
              ...node,
              originProjectId: node.projectId,
              uniqueId: childId,
              isQuoteScenarioStep: true,
              isRefScenarioStep: true, // 默认是完全引用的
              draggable: false,
            };
          }),
          id,
          uniqueId: id,
          originProjectId: e.projectId,
          draggable: false,
        };
      });
    }
    return fullScenarioArr;
  }

  const folderTree = ref<ModuleTreeNode[]>([]);
  const modulesCount = ref<Record<string, any>>({});

  const selectedModuleProps = ref({
    modulesTree: folderTree.value,
    moduleCount: modulesCount.value,
  });

  const activeTypeKey = computed(() => activeKey.value.toLocaleUpperCase());

  function initModuleTreeData(tree: ModuleTreeNode[], moduleCount: Record<string, any>) {
    folderTree.value = tree;
    modulesCount.value = moduleCount;
    selectedModuleProps.value.modulesTree = tree;
    selectedModuleProps.value.moduleCount = moduleCount;
  }

  const apiUseTreeSelection = useTreeSelection(selectedModuleProps.value);

  const caseUseTreeSelection = useTreeSelection(selectedModuleProps.value);

  const scenarioUseTreeSelection = useTreeSelection(selectedModuleProps.value);

  const useTreeHooksMap = ref<Record<string, any>>({
    API: apiUseTreeSelection,
    CASE: caseUseTreeSelection,
    SCENARIO: scenarioUseTreeSelection,
  });

  const apiTotal = computed(() => apiUseTreeSelection.totalCount.value || 0);
  const caseTotal = computed(() => caseUseTreeSelection.totalCount.value || 0);
  const scenarioTotal = computed(() => scenarioUseTreeSelection.totalCount.value || 0);
  const totalCount = computed(() => apiTotal.value + caseTotal.value + scenarioTotal.value);

  const currentUseTreeSelection = ref();

  watch(
    () => activeTypeKey.value,
    (val) => {
      currentUseTreeSelection.value = useTreeHooksMap.value[val];
    },
    {
      deep: true,
      immediate: true,
    }
  );

  watch(
    () => folderTree.value,
    (val) => {
      if (val) {
        selectedModuleProps.value.modulesTree = val;
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => modulesCount.value,
    (val) => {
      if (val) {
        selectedModuleProps.value.moduleCount = val;
      }
    },
    {
      immediate: true,
    }
  );

  function getMapParams(modulesMaps: Record<string, moduleKeysType>) {
    const selectedParams: Record<string, saveParams> = {};
    Object.entries(modulesMaps).forEach(([moduleId, selectedProps]) => {
      const { selectAll, selectIds, excludeIds, count } = selectedProps;
      selectedParams[moduleId] = {
        count,
        selectAll,
        selectIds: [...selectIds],
        excludeIds: [...excludeIds],
      };
    });
    return selectedParams;
  }

  function makeParams(type: ScenarioStepRefType.COPY | ScenarioStepRefType.REF) {
    const params: ImportSystemData = {
      API: {
        scenarioId: props.scenarioId,
        moduleMaps: getMapParams(apiUseTreeSelection.selectedModulesMaps.value),
        selectAllModule: apiUseTreeSelection.isCheckedAll.value,
        refType: type,
        projectId: currentProject.value,
        protocols: protocolsParam.value,
      },
      CASE: {
        scenarioId: props.scenarioId,
        moduleMaps: getMapParams(caseUseTreeSelection.selectedModulesMaps.value),
        selectAllModule: caseUseTreeSelection.isCheckedAll.value,
        refType: type,
        projectId: currentProject.value,
        protocols: protocolsParam.value,
      },
      SCENARIO: {
        scenarioId: props.scenarioId,
        moduleMaps: getMapParams(scenarioUseTreeSelection.selectedModulesMaps.value),
        selectAllModule: scenarioUseTreeSelection.isCheckedAll.value,
        refType: type,
        projectId: currentProject.value,
        protocols: protocolsParam.value,
      },
    };
    return params;
  }

  const insertTree = ref([]);
  async function getImportStepTree(params: ImportSystemData) {
    try {
      loading.value = true;
      insertTree.value = await scenarioAssociateExport(params);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function handleCopyOrQuote(refType: ScenarioStepRefType.COPY | ScenarioStepRefType.REF) {
    const params = makeParams(refType);
    await getImportStepTree(params);
    const copyApis: MsTableDataItem<ApiDefinitionDetail>[] = insertTree.value.filter(
      (item: any) => item.stepType === 'API'
    );
    const copyCases: MsTableDataItem<ApiCaseDetail>[] = insertTree.value.filter(
      (item: any) => item.stepType === 'API_CASE'
    );
    const selectedScenario: MsTableDataItem<ApiScenarioTableItem>[] = insertTree.value.filter(
      (item: any) => item.stepType === 'API_SCENARIO'
    );

    let fullScenarioArr: MsTableDataItem<ApiScenarioTableItem>[] = [];

    if (selectedScenario.length > 0) {
      fullScenarioArr = getScenarioSteps(refType, selectedScenario);
    }

    if (refType === ScenarioStepRefType.COPY) {
      emit(
        'copy',
        cloneDeep({
          api: copyApis,
          case: copyCases,
          scenario: fullScenarioArr,
        })
      );
    } else {
      emit(
        'quote',
        cloneDeep({
          api: copyApis,
          case: copyCases,
          scenario: fullScenarioArr,
        })
      );
    }

    handleCancel();
  }

  // 清空全部
  function clearSelected() {
    apiUseTreeSelection.clearSelector();
    caseUseTreeSelection.clearSelector();
    scenarioUseTreeSelection.clearSelector();
  }

  function handleChangeProject() {
    clearSelected();
    initModuleTree();
  }

  onBeforeMount(async () => {
    await initProtocolList();
    const localProtocol = getLocalStorage<string>(ProtocolKeyEnum.API_SCENARIO_IMPORT_PROTOCOL);
    if (localProtocol?.length && protocolOptions.value.some((item) => item.value === localProtocol)) {
      protocol.value = localProtocol;
    } else {
      protocol.value = 'HTTP';
    }
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
  }
</style>
