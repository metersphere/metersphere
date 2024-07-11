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
    <div class="flex h-[calc(100vh-118px)]">
      <div class="w-[292px] border-r border-[var(--color-text-n8)] p-[16px]">
        <CaseTree
          ref="caseTreeRef"
          v-model:checkedKeys="checkedKeys"
          v-model:selected-keys="selectedKeys"
          v-model:halfCheckedKeys="halfCheckedKeys"
          v-model:isCheckedAll="isCheckedAll"
          v-model:indeterminate="indeterminate"
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
          @check-all-module="checkAllModule"
        />
      </div>
      <div class="relative flex w-[calc(100%-293px)] flex-col p-[16px]">
        <MsAdvanceFilter
          v-model:keyword="keyword"
          :filter-config-list="[]"
          :custom-fields-config-list="[]"
          :row-count="0"
          :search-placeholder="searchPlaceholder"
          @keyword-search="loadCaseList"
          @adv-search="loadCaseList"
          @refresh="loadCaseList"
        >
          <template #left>
            <div class="flex w-full items-center justify-between">
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
          @get-module-count="initModulesCount"
          @refresh="loadCaseList"
        >
          <TotalCount :total-count="totalCount" />
        </CaseTable>
        <!-- 接口用例 API -->
        <ApiTable
          v-if="associationType === CaseLinkEnum.API && showType === 'API'"
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
          :protocols="selectedProtocols"
          :module-tree="moduleTree"
          :modules-count="modulesCount"
          @get-module-count="initModulesCount"
        >
          <TotalCount :total-count="totalCount" />
        </ApiTable>
        <!-- 接口用例 CASE -->
        <ApiCaseTable
          v-if="associationType === CaseLinkEnum.API && showType === 'CASE'"
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
          :protocols="selectedProtocols"
          :module-tree="moduleTree"
          :modules-count="modulesCount"
          @get-module-count="initModulesCount"
        >
          <TotalCount :total-count="totalCount" />
        </ApiCaseTable>
        <!-- 接口场景用例 -->
        <ScenarioCaseTable
          v-if="associationType === CaseLinkEnum.SCENARIO"
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
          @get-module-count="initModulesCount"
          @refresh="loadCaseList"
        >
          <TotalCount :total-count="totalCount" />
        </ScenarioCaseTable>
      </div>
    </div>
    <div class="footer !ml-[10px] w-[calc(100%-10px)]">
      <div class="flex items-center">
        <slot name="footerLeft">
          <div v-if="props.associatedType === CaseLinkEnum.FUNCTIONAL" class="flex items-center">
            <a-switch v-model:model-value="syncCase" size="small" type="line" />
            <div class="ml-[8px]">{{ t('ms.case.associate.syncFunctionalCase') }}</div>
            <a-tooltip :content="t('ms.case.associate.addAutomaticallyCase')" position="top">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
          <div v-if="props.associatedType === CaseLinkEnum.FUNCTIONAL" class="ml-[16px] flex items-center">
            <a-tree-select
              v-model="apiCaseCollectionId"
              :field-names="{
                title: 'name',
                key: 'id',
                children: 'children',
              }"
              class="w-[200px]"
              :data="apiSetTree"
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
              :data="scenarioSetTree"
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
          <a-button
            :loading="props.confirmLoading"
            type="primary"
            :disabled="!isDisabledSaveButton"
            @click="handleConfirm"
          >
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

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import ApiCaseTable from './apiCaseTable.vue';
  import ApiTable from './apiTable.vue';
  import CaseTable from './caseTable.vue';
  import CaseTree from './caseTree.vue';
  import ScenarioCaseTable from './scenarioCaseTable.vue';
  import TotalCount from './totalCount.vue';

  import { getAssociatedProjectOptions } from '@/api/modules/case-management/featureCase';
  import { getApiCaseModule, getApiScenarioModule } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';

  import type { ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type { ProjectListItem } from '@/models/setting/project';
  import { CaseModulesApiTypeEnum, CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  import type { moduleKeysType, saveParams } from './types';
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
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:projectId', val: string): void;
    (e: 'update:currentSelectCase', val: string | number | Record<string, any> | undefined): void;
    (e: 'init', val: TableQueryParams): void; // 初始化模块数量
    (e: 'close'): void;
    (e: 'save', params: any): void; // 保存对外传递关联table 相关参数
  }>();

  const keyword = ref<string>('');

  const projectList = ref<ProjectListItem[]>([]);
  const innerProject = useVModel(props, 'projectId', emit);

  const showType = ref('API');
  const innerVisible = useVModel(props, 'visible', emit);

  const associateType = ref<string>('project');

  const modulesCount = ref<Record<string, any>>({});
  const associationType = ref<keyof typeof CaseLinkEnum>('FUNCTIONAL');

  const activeFolder = ref('all');
  const selectedIds = ref<string[]>([]);

  const checkedKeys = ref<Array<string | number>>([]);
  const halfCheckedKeys = ref<Array<string | number>>([]);

  // 数据集合
  const selectedModulesMaps = ref<Record<string, moduleKeysType>>({});

  // 计算用例是否选择禁用关联按钮
  const isDisabledSaveButton = computed(() => {
    return Object.values(selectedModulesMaps.value).some((module) => module.selectAll || module.selectIds.size > 0);
  });

  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });

  const syncCase = ref<boolean>(true);

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

  const isCheckedAll = ref<boolean>(false);
  const indeterminate = ref<boolean>(false);

  // TODO 需优化、手动触发计算、或者每次增量更新
  const totalCount = computed(() => {
    return Object.keys(selectedModulesMaps.value).reduce((total, key) => {
      const module = selectedModulesMaps.value[key];
      if (key !== 'all') {
        // 未全选存在排除则要 count总-排除掉的 = 已选
        if (module.excludeIds.size && !module.selectAll) {
          total += module.count - module.excludeIds.size;
          // 已全选未排除则要+count
        } else if (module.selectAll && !module.excludeIds.size) {
          total += module.count;
          // 未全选则 + 选择的id集合
        } else if (!module.selectAll && module.selectIds.size) {
          total += module.selectIds.size;
        }
      }
      return total;
    }, 0);
  });

  const apiCaseCollectionId = ref<string>('');
  const apiScenarioCollectionId = ref<string>('');

  // 切换项目的时候清空
  function clearSelector() {
    Object.keys(selectedModulesMaps.value).forEach((key) => {
      delete selectedModulesMaps.value[key];
    });
  }

  // 保存
  function handleConfirm() {
    const params = {
      moduleMaps: getMapParams(),
      syncCase: syncCase.value,
      apiCaseCollectionId: apiCaseCollectionId.value,
      apiScenarioCollectionId: apiScenarioCollectionId.value,
      selectAllModule: isCheckedAll.value,
      projectId: innerProject.value,
      associateType: 'FUNCTIONAL',
      totalCount: totalCount.value,
    };

    if (params.associateType === CaseLinkEnum.API) {
      params.associateType = showType.value;
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

  const selectedProtocols = ref<string[]>([]);
  async function initModulesCount(params: TableQueryParams) {
    try {
      modulesCount.value = await initGetModuleCountFunc(props.getModuleCountApiType, associationType.value, {
        ...params,
        moduleIds: [],
        filter: {},
        keyword: '',
        ...props.extraModuleCountParams,
        protocols: associationType.value === CaseLinkEnum.API ? selectedProtocols.value : undefined,
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
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

  const moduleTree = ref<ModuleTreeNode[]>([]);
  function initModuleTree(tree: ModuleTreeNode[], _protocols?: string[]) {
    moduleTree.value = tree;
    selectedProtocols.value = _protocols || [];
    if (props.associatedType === CaseLinkEnum.API) {
      loadCaseList();
    }
  }

  const apiSetTree = ref<ModuleTreeNode[]>();
  const scenarioSetTree = ref<ModuleTreeNode[]>();

  async function initTestSet() {
    if (props.extraTableParams?.testPlanId) {
      try {
        apiSetTree.value = await getApiCaseModule({
          testPlanId: props.extraTableParams.testPlanId,
          treeType: 'COLLECTION',
        });
        scenarioSetTree.value = await getApiScenarioModule({
          testPlanId: props.extraTableParams.testPlanId,
          treeType: 'COLLECTION',
        });
      } catch (error) {
        console.log(error);
      }
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
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        associationType.value = props.associatedType;
        activeFolder.value = 'all';
        initProjectList();
        initTestSet();
      }
      selectPopVisible.value = false;
      keyword.value = '';
      selectedIds.value = [];
    }
  );

  // 选中当前节点 && 取消当前节点
  function selectParent(nodeData: MsTreeNodeData, isSelected: boolean) {
    selectedModulesMaps.value[nodeData.id] = {
      selectAll: !isSelected,
      selectIds: new Set(),
      excludeIds: new Set(),
      count: nodeData.count,
    };
  }

  // 初始化左侧模块节点选中当前以及子节点
  function processAllCurrentNode(node: MsTreeNodeData, check: boolean) {
    if (node.children && node.children.length) {
      node.children?.forEach((childrenNode: MsTreeNodeData) => processAllCurrentNode(childrenNode, check));
    }
    selectedModulesMaps.value[node.id] = {
      selectAll: check,
      selectIds: new Set(),
      excludeIds: new Set(),
      count: node.count,
    };
  }

  // 选中当前节点以及子节点
  function checkNode(_checkedKeys: Array<string | number>, checkedData: MsTreeNodeData) {
    const { checked, node } = checkedData;
    processAllCurrentNode(node, checked);
  }

  watch(
    () => selectedModulesMaps.value,
    (val) => {
      const checkedKeysSet = new Set(checkedKeys.value);
      const halfCheckedKeysSet = new Set(halfCheckedKeys.value);

      if (!Object.keys(val).length) {
        checkedKeysSet.clear();
        halfCheckedKeysSet.clear();
        isCheckedAll.value = false;
        indeterminate.value = false;
      }

      Object.entries(val).forEach(([moduleId, selectedProps]) => {
        const { selectAll: selectIdsAll, selectIds, count, excludeIds } = selectedProps;
        if (selectedProps) {
          // 全选和取消全选
          if (selectIdsAll) {
            checkedKeysSet.add(moduleId);
          } else {
            checkedKeysSet.delete(moduleId);
          }

          // 半选状态
          if (excludeIds.size || (selectIds.size > 0 && selectIds.size < count)) {
            halfCheckedKeysSet.add(moduleId);
          } else {
            halfCheckedKeysSet.delete(moduleId);
          }
        }
      });

      // 更新 checkedKeys 和 halfCheckedKeys
      checkedKeys.value = Array.from(checkedKeysSet);
      halfCheckedKeys.value = Array.from(halfCheckedKeysSet);

      // 更新全选和半选状态
      const isAllCheckedModuleProps = val.all;

      if (isAllCheckedModuleProps) {
        if (totalCount.value === isAllCheckedModuleProps.count) {
          isCheckedAll.value = true;
        } else {
          isCheckedAll.value = false;
        }

        if (totalCount.value === 0) {
          indeterminate.value = false;
        } else if (totalCount.value < isAllCheckedModuleProps.count) {
          indeterminate.value = true;
        } else {
          indeterminate.value = false;
        }
      }
    },
    { deep: true }
  );

  // 全选全部&取消全选
  function setSelectAll(tree: MsTreeNodeData[], checkedAll: boolean) {
    tree.forEach((node) => {
      processAllCurrentNode(node, checkedAll);

      if (node.children) {
        setSelectAll(node.children, checkedAll);
      }
    });
  }

  function checkAllModule() {
    selectedModulesMaps.value.all = {
      selectAll: isCheckedAll.value,
      selectIds: new Set(),
      excludeIds: new Set(),
      count: modulesCount.value.all,
    };

    setSelectAll(moduleTree.value, isCheckedAll.value);

    const lastProps = selectedModulesMaps.value.all;
    if (lastProps.selectAll) {
      selectedModulesMaps.value.all.selectAll = true;
      selectedModulesMaps.value.all.selectIds = new Set([]);
      selectedModulesMaps.value.all.excludeIds = new Set([]);
    } else {
      selectedModulesMaps.value.all.selectAll = false;
      selectedModulesMaps.value.all.selectIds = new Set([]);
      selectedModulesMaps.value.all.excludeIds = new Set([]);
    }
  }

  watch([() => innerProject.value, () => showType.value, () => props.associatedType], (val) => {
    if (val) {
      clearSelector();
    }
  });

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
      }
    }
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
