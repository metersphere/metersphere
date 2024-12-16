<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="t('ms.case.associate.title')"
    :width="1200"
    :footer="false"
    no-content-padding
    unmount-on-close
  >
    <template #headerLeft>
      <a-popconfirm
        v-if="!getIsVisited()"
        class="ms-pop-confirm--hidden-cancel change-project-pop"
        position="br"
        :popup-visible="selectPopVisible"
        popup-container="#typeSelectGroupRef"
        :ok-text="t('ms.case.associate.gotIt')"
        @ok="okHandler"
        @popup-visible-change="handlePopChange"
      >
        <div class="float-left">
          <a-input-group>
            <!-- TODO 这个版本不上 -->
            <!-- <a-select
              v-model="functionalType"
              class="ml-2 w-[100px]"
              :default-value="innerProject"
              :placeholder="t('common.pleaseSelect')"
            >
              <template #arrow-icon>
                <icon-caret-down />
              </template>
              <a-tooltip v-for="item of functionalList" :key="item.id" :mouse-enter-delay="500" :content="item.name">
                <a-option :value="item.id" :class="item.id === innerProject ? 'arco-select-option-selected' : ''">
                  {{ item.name }}
                </a-option>
              </a-tooltip>
            </a-select> -->

            <a-select
              v-model="innerProject"
              :popup-visible="selectVisible"
              class="ml-2 w-[240px]"
              :default-value="innerProject"
              :placeholder="t('common.pleaseSelect')"
              @popup-visible-change="changeProjectHandler"
            >
              <template #arrow-icon>
                <icon-caret-down />
              </template>
              <a-tooltip v-for="item of projectList" :key="item.id" :mouse-enter-delay="500" :content="item.name">
                <a-option :value="item.id" :class="item.id === innerProject ? 'arco-select-option-selected' : ''">
                  {{ item.name }}
                </a-option>
              </a-tooltip>
            </a-select>
          </a-input-group>
        </div>
        <template #cancel-text>
          <div> </div>
        </template>
        <template #icon>
          <MsIcon class="text-[rgb(var(--primary-5))]" type="icon-icon_warning_filled" size="16" />
        </template>
        <template #content>
          <div class="font-semibold text-[var(--color-text-1)]">
            {{ t('ms.case.associate.switchProject') }}
          </div>
          <div class="mt-[8px] w-[215px] text-[12px] leading-[16px] text-[var(--color-text-2)]">
            {{ t('ms.case.associate.switchProjectPopTip') }}
          </div>
        </template>
      </a-popconfirm>
      <div v-else class="float-left">
        <a-input-group>
          <!-- TODO 这个版本不上 -->
          <!-- <a-select
            v-model="functionalType"
            class="ml-2 w-[100px]"
            :default-value="innerProject"
            allow-search
            :placeholder="t('common.pleaseSelect')"
          >
            <template #arrow-icon>
              <icon-caret-down />
            </template>
            <a-tooltip v-for="item of functionalList" :key="item.id" :mouse-enter-delay="500" :content="item.name">
              <a-option :value="item.id" :class="item.id === innerProject ? 'arco-select-option-selected' : ''">
                {{ item.name }}
              </a-option>
            </a-tooltip>
          </a-select> -->

          <a-select
            id="typeRadioGroupRef"
            v-model:model-value="innerProject"
            :popup-visible="selectVisible"
            class="ml-2 w-[240px]"
            :default-value="innerProject"
            allow-search
            :placeholder="t('common.pleaseSelect')"
            @popup-visible-change="changeProjectHandler"
          >
            <template #arrow-icon>
              <icon-caret-down />
            </template>
            <a-tooltip v-for="item of projectList" :key="item.id" :mouse-enter-delay="500" :content="item.name">
              <a-option :value="item.id" :class="item.id === innerProject ? 'arco-select-option-selected' : ''">
                {{ item.name }}
              </a-option>
            </a-tooltip>
          </a-select>
        </a-input-group>
      </div>
    </template>
    <a-spin :loading="tableLoading" class="w-full">
      <div class="flex h-[calc(100vh-118px)]">
        <div v-show="!isAdvancedSearchMode" class="w-[292px] border-r border-[var(--color-text-n8)] p-[16px]">
          <CaseTree
            ref="caseTreeRef"
            v-model:checkedKeys="checkedKeys"
            v-model:selected-keys="selectedKeys"
            v-model:halfCheckedKeys="halfCheckedKeys"
            :modules-count="modulesCount"
            :get-modules-api-type="props.getModulesApiType"
            :current-project="innerProject"
            :active-tab="associationType"
            :extra-modules-params="props.extraModulesParams"
            :show-type="showType"
            :folder-name="folderName"
            @folder-node-select="handleFolderNodeSelect"
            @init="initModuleTree"
            @change-protocol="handleProtocolChange"
            @select-parent="selectParent"
            @check="checkNode"
          >
            <div class="flex items-center justify-between">
              <a-checkbox v-model:model-value="isCheckedAll" :indeterminate="indeterminate" @change="checkAllModule">
                {{ t('ms.case.associate.allData') }}
              </a-checkbox>
              <span class="pr-[8px] text-[var(--color-text-brand)]">
                {{ modulesCount.all }}
              </span>
            </div>
          </CaseTree>
        </div>
        <div :class="[`relative flex ${!isAdvancedSearchMode ? 'w-[calc(100%-293px)]' : 'w-full'} flex-col p-[16px]`]">
          <MsAdvanceFilter
            ref="msAdvanceFilterRef"
            v-model:keyword="keyword"
            :view-type="viewType"
            :filter-config-list="filterConfigList"
            :custom-fields-config-list="searchCustomFields"
            :search-placeholder="searchPlaceholder"
            @keyword-search="loadCaseList()"
            @adv-search="handleAdvSearch"
            @refresh="loadCaseList()"
          >
            <template #left>
              <div class="flex items-center">
                <a-radio-group
                  v-if="associationType === 'API'"
                  v-model="showType"
                  type="button"
                  class="file-show-type mr-2"
                >
                  <a-radio value="API" class="show-type-icon p-[2px]">API</a-radio>
                  <a-radio value="CASE" class="show-type-icon p-[2px]">CASE</a-radio>
                </a-radio-group>
                <a-popover v-else title="" position="bottom">
                  <div class="flex">
                    <div class="one-line-text mr-1 max-h-[32px] max-w-[300px] text-[var(--color-text-1)]">
                      {{ activeFolderName || folderName }}
                    </div>
                    <span class="text-[var(--color-text-4)]"> ({{ modulesCount[activeFolder] || 0 }})</span>
                  </div>
                  <template #content>
                    <div class="max-w-[400px] text-[14px] font-medium text-[var(--color-text-1)]">
                      {{ activeFolderName || folderName }}
                      <span class="text-[var(--color-text-4)]">({{ modulesCount[activeFolder] || 0 }})</span>
                    </div>
                  </template>
                </a-popover>
              </div>
            </template>
          </MsAdvanceFilter>
          <!-- 功能用例 -->
          <CaseTable
            v-if="associationType === CaseLinkEnum.FUNCTIONAL"
            ref="functionalTableRef"
            v-model:selectedIds="selectedIds"
            v-model:selectedModulesMaps="selectedModulesMaps"
            :is-advanced-search-mode="isAdvancedSearchMode"
            :association-type="associateType"
            :get-page-api-type="getPageApiType"
            :active-module="activeFolder"
            :offspring-ids="offspringIds"
            :current-project="innerProject"
            :associated-ids="props.associatedIds"
            :active-source-type="associationType"
            :extra-table-params="props.extraTableParams"
            :keyword="keyword"
            :module-tree="moduleTree"
            :modules-count="modulesCount"
            :test-plan-list="testPlanList"
            @get-module-count="initModulesCount"
            @refresh="loadCaseList"
          >
            <TotalCount :total-count="totalCount" />
          </CaseTable>
          <!-- 接口用例 API -->
          <ApiTable
            v-else-if="associationType === CaseLinkEnum.API && showType === 'API'"
            ref="apiTableRef"
            v-model:selectedIds="selectedIds"
            v-model:selectedModulesMaps="selectedModulesMaps"
            :get-page-api-type="getPageApiType"
            :extra-table-params="props.extraTableParams"
            :association-type="associateType"
            :active-module="activeFolder"
            :offspring-ids="offspringIds"
            :current-project="innerProject"
            :associated-ids="props.associatedIds"
            :active-source-type="associationType"
            :keyword="keyword"
            :show-type="showType"
            :is-advanced-search-mode="isAdvancedSearchMode"
            :all-protocol-list="allProtocolList"
            :protocols="selectedProtocols"
            :module-tree="moduleTree"
            :modules-count="modulesCount"
            :test-plan-list="testPlanList"
            @get-module-count="initModulesCount"
          >
            <TotalCount :total-count="totalCount" />
          </ApiTable>
          <!-- 接口用例 CASE -->
          <ApiCaseTable
            v-else-if="associationType === CaseLinkEnum.API && showType === 'CASE'"
            ref="caseTableRef"
            v-model:selectedIds="selectedIds"
            v-model:selectedModulesMaps="selectedModulesMaps"
            :get-page-api-type="getPageApiType"
            :extra-table-params="props.extraTableParams"
            :association-type="associateType"
            :active-module="activeFolder"
            :offspring-ids="offspringIds"
            :current-project="innerProject"
            :associated-ids="props.associatedIds"
            :active-source-type="associationType"
            :keyword="keyword"
            :show-type="showType"
            :is-advanced-search-mode="isAdvancedSearchMode"
            :all-protocol-list="allProtocolList"
            :protocols="selectedProtocols"
            :module-tree="moduleTree"
            :modules-count="modulesCount"
            :test-plan-list="testPlanList"
            @get-module-count="initModulesCount"
          >
            <TotalCount :total-count="totalCount" />
          </ApiCaseTable>
          <!-- 接口场景用例 -->
          <ScenarioCaseTable
            v-else-if="associationType === CaseLinkEnum.SCENARIO"
            ref="scenarioTableRef"
            v-model:selectedModulesMaps="selectedModulesMaps"
            v-model:selectedIds="selectedIds"
            :association-type="associateType"
            :modules-count="modulesCount"
            :active-module="activeFolder"
            :offspring-ids="offspringIds"
            :current-project="innerProject"
            :associated-ids="props.associatedIds"
            :active-source-type="associationType"
            :get-page-api-type="getPageApiType"
            :extra-table-params="props.extraTableParams"
            :keyword="keyword"
            :module-tree="moduleTree"
            :total-count="totalCount"
            :is-advanced-search-mode="isAdvancedSearchMode"
            :all-protocol-list="allProtocolList"
            :test-plan-list="testPlanList"
            @get-module-count="initModulesCount"
            @refresh="loadCaseList"
          >
            <TotalCount :total-count="totalCount" />
          </ScenarioCaseTable>
        </div>
      </div>
    </a-spin>
    <div class="footer !ml-[10px] w-[calc(100%-10px)]">
      <div class="flex items-center">
        <slot name="footerLeft">
          <div v-if="props.associatedType === CaseLinkEnum.FUNCTIONAL" class="flex items-center">
            <a-switch v-model:model-value="syncCase" size="small" type="line" @change="changeSyncCase" />
            <div class="ml-[8px]">{{ t('ms.case.associate.syncFunctionalCase') }}</div>
            <a-tooltip :content="t('ms.case.associate.addAutomaticallyCase')" position="top">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
          <div v-if="props.associatedType === CaseLinkEnum.FUNCTIONAL && syncCase" class="ml-[16px] flex items-center">
            <a-tree-select
              v-model="apiCaseCollectionId"
              :field-names="{
                title: 'name',
                key: 'id',
                children: 'children',
              }"
              class="w-[200px]"
              :data="nodeApiTestSet"
              allow-clear
            >
              <template #prefix>
                <div class="text-[var(--color-text-brand)]">{{ t('ms.case.associate.api') }}</div>
              </template>
              <template #tree-slot-title="node">
                <a-tooltip :content="`${node.name}`" position="tl">
                  <div class="one-line-text w-[180px]">{{ node.name }}</div>
                </a-tooltip>
              </template>
            </a-tree-select>

            <a-tree-select
              v-model="apiScenarioCollectionId"
              :field-names="{
                title: 'name',
                key: 'id',
                children: 'children',
              }"
              class="ml-[12px] w-[200px]"
              allow-clear
              :data="nodeScenarioTestSet"
            >
              <template #prefix>
                <div class="text-[var(--color-text-brand)]">{{ t('ms.case.associate.scenario') }}</div>
              </template>
              <template #tree-slot-title="node">
                <a-tooltip :content="`${node.name}`" position="tl">
                  <div class="one-line-text w-[180px]">{{ node.name }}</div>
                </a-tooltip>
              </template>
            </a-tree-select>
          </div>
        </slot>
      </div>
      <div class="flex items-center">
        <slot name="footerRight">
          <a-button type="secondary" :disabled="props.confirmLoading" class="mr-[12px]" @click="cancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button :loading="props.confirmLoading" type="primary" :disabled="!totalCount" @click="handleConfirm">
            {{ t('ms.case.associate.associate') }}
          </a-button>
        </slot>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { type SelectOptionData } from '@arco-design/web-vue';
  import { isEqual } from 'lodash-es';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import ApiCaseTable from './apiCaseTable.vue';
  import ApiTable from './apiTable.vue';
  import CaseTable from './caseTable.vue';
  import CaseTree from './caseTree.vue';
  import ScenarioCaseTable from './scenarioCaseTable.vue';
  import TotalCount from './totalCount.vue';

  import { getAssociatedProjectOptions } from '@/api/modules/case-management/featureCase';
  import { getTestPlanListWithoutPage } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';

  import type { ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type { ProjectListItem } from '@/models/setting/project';
  import type { AssociateCaseRequestParams } from '@/models/testPlan/testPlan';
  import { ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { CaseModulesApiTypeEnum, CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  import type { saveParams } from './types';
  import useTreeSelection from './useTreeSelection';
  import { initGetModuleCountFunc } from './utils/moduleCount';

  const visitedKey = 'changeLinkProject';
  const { addVisited, getIsVisited } = useVisit(visitedKey);

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    projectId: string; // 项目id
    caseId?: string; // 用例id
    getModulesApiType: CaseModulesApiTypeEnum[keyof CaseModulesApiTypeEnum]; // 获取模块树Api
    extraModulesParams?: Record<string, any>; // 获取模块树请求额外参数
    getPageApiType: keyof typeof CasePageApiTypeEnum; // 获取未关联分页Api
    extraTableParams?: TableQueryParams; // 查询表格的额外参数
    getModuleCountApiType: CasePageApiTypeEnum[keyof CasePageApiTypeEnum]; // 获取模块count分页Api
    extraModuleCountParams?: TableQueryParams; // 查询模块数量额外参数
    okButtonDisabled?: boolean; // 确认按钮是否禁用
    confirmLoading?: boolean;
    modulesMaps?: Record<string, saveParams>;
    associatedIds?: string[]; // 已关联用例id集合用于去重已关联
    hideProjectSelect?: boolean; // 是否隐藏项目选择
    associatedType: keyof typeof CaseLinkEnum; // 关联类型
    protocols?: string[]; // 上一次选择的协议
    nodeApiTestSet?: SelectOptionData[]; // 接口测试集
    nodeScenarioTestSet?: SelectOptionData[]; // 场景测试集
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:projectId', val: string): void;
    (e: 'update:currentSelectCase', val: string | number | Record<string, any> | undefined): void;
    (e: 'init', val: TableQueryParams): void; // 初始化模块数量
    (e: 'close'): void;
    (e: 'save', params: any): void; // 保存对外传递关联table 相关参数
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
  }>();

  const keyword = ref<string>('');

  const projectList = ref<ProjectListItem[]>([]);
  const innerProject = useVModel(props, 'projectId', emit);

  const showType = ref<'API' | 'CASE'>('API');
  const innerVisible = useVModel(props, 'visible', emit);

  const associateType = ref<string>('project');

  const modulesCount = ref<Record<string, any>>({});
  const associationType = ref<keyof typeof CaseLinkEnum>('FUNCTIONAL');

  const activeFolder = ref('all');
  const selectedIds = ref<string[]>([]);

  const moduleTree = ref<ModuleTreeNode[]>([]);

  const selectedModuleProps = ref({
    modulesTree: moduleTree.value,
    moduleCount: modulesCount.value,
  });

  const {
    selectedModulesMaps,
    checkedKeys,
    halfCheckedKeys,
    isCheckedAll,
    indeterminate,
    selectParent,
    checkNode,
    checkAllModule,
    totalCount,
    clearSelector,
  } = useTreeSelection(selectedModuleProps.value);

  watch(
    () => moduleTree.value,
    (val) => {
      if (val) {
        if (val) {
          selectedModuleProps.value.modulesTree = val;
        }
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => modulesCount.value,
    (val) => {
      selectedModuleProps.value.moduleCount = val;
    },
    {
      immediate: true,
    }
  );

  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });

  const syncCase = ref<boolean>(false);

  const folderName = computed(() => {
    switch (associationType.value) {
      case CaseLinkEnum.FUNCTIONAL:
        return t('caseManagement.caseReview.allCases');
      case CaseLinkEnum.API:
        return t('apiTestManagement.allApi');
      case CaseLinkEnum.SCENARIO:
        return t('apiScenario.allScenario');
      default:
        return '';
    }
  });

  const searchPlaceholder = computed(() => {
    switch (associationType.value) {
      case CaseLinkEnum.FUNCTIONAL:
        return t('common.searchByIDNameTag');
      case CaseLinkEnum.API:
        return t('ms.case.associate.apiSearchPlaceholder');
      case CaseLinkEnum.SCENARIO:
        return t('common.searchByIDNameTag');
      default:
        return '';
    }
  });

  /**
   * 处理模块树节点选中事件
   */
  const offspringIds = ref<string[]>([]);
  const activeFolderName = ref('');

  function handleFolderNodeSelect(ids: string[], _offspringIds: string[], name?: string) {
    [activeFolder.value] = ids;
    offspringIds.value = [..._offspringIds];
    activeFolderName.value = name ?? '';
  }

  const functionalTableRef = ref<InstanceType<typeof CaseTable>>();
  const apiTableRef = ref<InstanceType<typeof ApiTable>>();
  const caseTableRef = ref<InstanceType<typeof ApiCaseTable>>();
  const scenarioTableRef = ref<InstanceType<typeof ScenarioCaseTable>>();

  function getMapParams() {
    const selectedParams: Record<string, saveParams> = {};
    Object.entries(selectedModulesMaps.value).forEach(([moduleId, selectedProps]) => {
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

  const apiCaseCollectionId = ref<string>('');
  const apiScenarioCollectionId = ref<string>('');
  const selectedProtocols = ref<string[]>([]);
  // 保存
  function handleConfirm() {
    const params: AssociateCaseRequestParams = {
      moduleMaps: getMapParams(),
      syncCase: false,
      apiCaseCollectionId: '',
      apiScenarioCollectionId: '',
      selectAllModule: isCheckedAll.value,
      projectId: innerProject.value,
      associateType: 'FUNCTIONAL',
      totalCount: totalCount.value,
      protocols: [],
    };
    // 只有关联功能用例才有关联接口用例测试集和场景用例测试集且如果关闭则清空已选择测试集
    if (props.associatedType === CaseLinkEnum.FUNCTIONAL && syncCase.value) {
      params.apiCaseCollectionId = apiCaseCollectionId.value;
      params.apiScenarioCollectionId = apiScenarioCollectionId.value;
      params.syncCase = syncCase.value;
    }

    if (props.associatedType === CaseLinkEnum.API) {
      params.associateType = showType.value === 'API' ? showType.value : 'API_CASE';
      params.protocols = selectedProtocols.value;
    } else {
      params.associateType = props.associatedType;
    }
    emit('save', params);
  }

  function cancel() {
    innerVisible.value = false;
    keyword.value = '';
    activeFolder.value = 'all';
    activeFolderName.value = t('ms.case.associate.allCase');
    clearSelector();
    emit('close');
  }

  async function initProjectList() {
    try {
      projectList.value = await getAssociatedProjectOptions(appStore.currentOrgId, associationType.value);
      const hasProjectPermission = projectList.value.map((item) => item.id).includes(appStore.currentProjectId);
      // 如果项目模块未开启则不选中它该项目
      if (hasProjectPermission) {
        innerProject.value = appStore.currentProjectId;
      } else {
        innerProject.value = projectList.value[0].id;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const tableLoading = ref(false);
  async function initModulesCount(params: TableQueryParams) {
    try {
      tableLoading.value = true;
      modulesCount.value = await initGetModuleCountFunc(
        props.getModuleCountApiType,
        associationType.value,
        showType.value,
        {
          ...params,
          moduleIds: [],
          filter: {},
          keyword: '',
          ...props.extraModuleCountParams,
          protocols: associationType.value === CaseLinkEnum.API ? selectedProtocols.value : undefined,
        }
      );
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      tableLoading.value = false;
    }
  }

  const selectVisible = ref<boolean>(false);
  const selectPopVisible = ref<boolean>(false);

  function loadCaseList() {
    if (props.associatedIds && props.associatedIds.length > 0) {
      selectedIds.value = props.associatedIds;
    }
    switch (associationType.value) {
      case CaseLinkEnum.FUNCTIONAL:
        return functionalTableRef.value?.loadCaseList();
      case CaseLinkEnum.API:
        return showType.value === 'API' ? apiTableRef.value?.loadApiList() : caseTableRef.value?.loadCaseList();
      case CaseLinkEnum.SCENARIO:
        return scenarioTableRef.value?.loadScenarioList();
      default:
        break;
    }
  }
  const caseTreeRef = ref<InstanceType<typeof CaseTree>>();
  const allProtocolList = computed<string[]>(() => caseTreeRef.value?.allProtocolList ?? []);

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);

  const viewType = computed(() => {
    switch (associationType.value) {
      case CaseLinkEnum.FUNCTIONAL:
        return ViewTypeEnum.PLAN_FUNCTIONAL_CASE_DRAWER;
      case CaseLinkEnum.API:
        return showType.value === 'API' ? ViewTypeEnum.PLAN_API_DEFINITION_DRAWER : ViewTypeEnum.PLAN_API_CASE_DRAWER;
      case CaseLinkEnum.SCENARIO:
        return ViewTypeEnum.PLAN_API_SCENARIO_DRAWER;
      default:
        return ViewTypeEnum.PLAN_FUNCTIONAL_CASE_DRAWER;
    }
  });
  function setAdvanceFilter(filter: FilterResult, id: string) {
    switch (associationType.value) {
      case CaseLinkEnum.FUNCTIONAL:
        return functionalTableRef.value?.setCaseAdvanceFilter(filter, id);
      case CaseLinkEnum.API:
        return showType.value === 'API'
          ? apiTableRef.value?.setCaseAdvanceFilter(filter, id)
          : caseTableRef.value?.setCaseAdvanceFilter(filter, id);
      case CaseLinkEnum.SCENARIO:
        return scenarioTableRef.value?.setCaseAdvanceFilter(filter, id);
      default:
        break;
    }
  }
  const filterConfigList = computed<FilterFormItem[]>(() => {
    switch (associationType.value) {
      case CaseLinkEnum.FUNCTIONAL:
        return functionalTableRef.value?.filterConfigList ?? [];
      case CaseLinkEnum.API:
        return (
          (showType.value === 'API' ? apiTableRef.value?.filterConfigList : caseTableRef.value?.filterConfigList) ?? []
        );
      case CaseLinkEnum.SCENARIO:
        return scenarioTableRef.value?.filterConfigList ?? [];
      default:
        return [];
    }
  });
  const searchCustomFields = computed(() => {
    if (associationType.value === CaseLinkEnum.FUNCTIONAL) {
      return functionalTableRef.value?.searchCustomFields;
    }
    return [];
  });
  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string) => {
    keyword.value = '';
    caseTreeRef.value?.setActiveFolder('all');
    setAdvanceFilter(filter, id);
    await loadCaseList(); // 基础筛选都清空
  };

  function initModuleTree(tree: ModuleTreeNode[], _protocols?: string[]) {
    moduleTree.value = tree;
    selectedProtocols.value = _protocols || [];
  }

  function changeSyncCase(value: string | number | boolean) {
    if (value) {
      if (props.nodeApiTestSet && props.nodeScenarioTestSet) {
        apiCaseCollectionId.value = props.nodeApiTestSet?.[0]?.id ?? '';
        apiScenarioCollectionId.value = props.nodeScenarioTestSet?.[0]?.id ?? '';
      }
    } else {
      apiCaseCollectionId.value = '';
      apiScenarioCollectionId.value = '';
    }
  }

  function changeProjectHandler(visible: boolean) {
    if (visible && !getIsVisited()) {
      selectPopVisible.value = true;
    } else {
      selectPopVisible.value = false;
      selectVisible.value = visible;
    }
  }

  function handlePopChange(visible: boolean) {
    if (visible) {
      selectPopVisible.value = false;
    }
  }

  function okHandler() {
    addVisited();
    selectPopVisible.value = false;
    selectVisible.value = true;
  }

  function handleProtocolChange(val: string[]) {
    selectedProtocols.value = val;
    // 如果外边未保存则初始化上来协议改变不清空，再次改变协议的时候清空上一次的选择结果
    nextTick(() => {
      if (!isEqual(props.protocols, selectedProtocols.value)) {
        clearSelector();
      }
    });
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        associationType.value = props.associatedType;
        activeFolder.value = 'all';
        initProjectList();
      }
      selectPopVisible.value = false;
      keyword.value = '';
      selectedIds.value = [];
      showType.value = 'API';
    }
  );

  watch(
    () => props.modulesMaps,
    (val) => {
      if (val && Object.keys(val).length) {
        Object.entries(val).forEach(([moduleId, selectedProps]) => {
          const { selectAll, selectIds, excludeIds, count } = selectedProps;
          selectedModulesMaps.value[moduleId] = {
            selectAll,
            count,
            selectIds: new Set(selectIds),
            excludeIds: new Set(excludeIds),
          };
        });
        // 保存后不再回显，则清空
      } else {
        clearSelector();
      }
    }
  );

  watch([() => innerProject.value, () => showType.value, () => props.associatedType], (val) => {
    if (val) {
      clearSelector();
    }
  });
  watch([() => innerProject.value, () => showType.value], (val) => {
    if (val) {
      msAdvanceFilterRef.value?.clearFilter();
    }
  });

  const testPlanList = ref<SelectOptionData[]>([]);
  async function getTestPlanList() {
    try {
      const res = await getTestPlanListWithoutPage(innerProject.value);
      testPlanList.value = res.map((item) => ({ value: item.id, label: `[${item.num}] ${item.name}` }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => innerProject.value,
    () => {
      getTestPlanList();
    },
    { immediate: true }
  );
</script>

<style scoped lang="less">
  .folder {
    @apply flex cursor-pointer items-center justify-between;

    padding: 8px 4px;
    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-text {
      @apply flex cursor-pointer items-center;
      .folder-icon {
        margin-right: 4px;
        color: var(--color-text-4);
      }
      .folder-name {
        color: var(--color-text-1);
      }
      .folder-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
    .folder-text--active {
      .folder-icon,
      .folder-name,
      .folder-count {
        color: rgb(var(--primary-5));
      }
    }
  }
  .footer {
    @apply flex items-center justify-between;

    margin: auto -16px -16px;
    padding: 12px 16px;
    box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
  }
  .expand-btn {
    padding: 8px;
    .arco-icon {
      color: var(--color-text-4);
    }
    &:hover {
      border-color: rgb(var(--primary-5)) !important;
      background-color: rgb(var(--primary-1)) !important;
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
  :deep(.test-set-form-item) {
    margin-bottom: 0;
    .test-set {
      border: 1px solid var(--color-text-n8);
      border-right: none;
      @apply flex items-center justify-center;
    }
  }
</style>

<style lang="less">
  .change-project-pop {
    .arco-trigger-popup-wrapper {
      .arco-popconfirm-popup-content {
        width: 215px !important;
        border: none;
      }
    }
  }
</style>
