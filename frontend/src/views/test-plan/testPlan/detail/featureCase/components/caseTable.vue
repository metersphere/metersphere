<template>
  <div class="h-full p-[16px]">
    <div class="mb-[16px]">
      <MsAdvanceFilter
        ref="msAdvanceFilterRef"
        v-model:keyword="keyword"
        :view-type="ViewTypeEnum.PLAN_API_SCENARIO"
        :filter-config-list="filterConfigList"
        :custom-fields-config-list="searchCustomFields"
        :search-placeholder="t('common.searchByIdName')"
        @keyword-search="loadCaseList()"
        @adv-search="handleAdvSearch"
        @refresh="handleRefreshAll()"
      >
        <template #right>
          <a-radio-group
            v-model:model-value="showType"
            type="button"
            size="small"
            class="list-show-type"
            @change="handleShowTypeChange"
          >
            <a-radio value="list" class="show-type-icon !m-[2px]">
              <MsIcon :size="14" type="icon-icon_view-list_outlined" />
            </a-radio>
            <a-radio value="minder" class="show-type-icon !m-[2px]">
              <MsIcon :size="14" type="icon-icon_mindnote_outlined" />
            </a-radio>
          </a-radio-group>
        </template>
      </MsAdvanceFilter>
    </div>
    <template v-if="showType === 'list'">
      <MsBaseTable
        ref="tableRef"
        v-bind="propsRes"
        :action-config="batchActions"
        :selectable="hasOperationPermission"
        :not-show-table-filter="isAdvancedSearchMode"
        v-on="propsEvent"
        @batch-action="handleTableBatch"
        @drag-change="handleDragChange"
        @selected-change="handleTableSelect"
        @filter-change="getModuleCount"
        @sorter-change="handleSorterChange"
      >
        <template #num="{ record }">
          <MsButton type="text" @click="toCaseDetail(record)">{{ record.num }}</MsButton>
        </template>
        <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
          <CaseLevel :case-level="filterContent.value" />
        </template>
        <template #caseLevel="{ record }">
          <CaseLevel :case-level="record.caseLevel" />
        </template>
        <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
          <ExecuteResult :execute-result="filterContent.key" />
        </template>
        <template #lastExecResult="{ record }">
          <a-select
            v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE']) && props.canEdit"
            v-model:model-value="record.lastExecResult"
            :placeholder="t('common.pleaseSelect')"
            class="param-input w-full"
            @change="() => handleEditLastExecResult(record)"
          >
            <template #label>
              <span class="text-[var(--color-text-2)]">
                <ExecuteResult :execute-result="record.lastExecResult" />
              </span>
            </template>
            <a-option v-for="item in Object.values(executionResultMap)" :key="item.key" :value="item.key">
              <ExecuteResult :execute-result="item.key" />
            </a-option>
          </a-select>
          <span v-else class="text-[var(--color-text-2)]">
            <ExecuteResult :execute-result="record.lastExecResult" />
          </span>
        </template>
        <template #bugCount="{ record }">
          <MsBugOperation
            :case-type="CaseLinkEnum.FUNCTIONAL"
            :can-edit="props.canEdit"
            :bug-list="record.bugList"
            :resource-id="record.id"
            :bug-count="record.bugCount || 0"
            :existed-defect="existedDefect"
            :permission="['PROJECT_TEST_PLAN:READ+EXECUTE']"
            @load-list="refreshList()"
            @associated="associateAndCreateDefect(true, false, record)"
            @create="associateAndCreateDefect(false, false, record)"
          />
        </template>
        <template v-if="props.canEdit" #operation="{ record }">
          <MsButton
            v-permission="['PROJECT_TEST_PLAN:READ+EXECUTE']"
            type="text"
            class="!mr-0"
            @click="toCaseDetail(record)"
          >
            {{ t('common.execute') }}
          </MsButton>
          <a-divider
            v-if="hasAllPermission(['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN:READ+ASSOCIATION'])"
            direction="vertical"
            :margin="8"
          ></a-divider>
          <MsPopconfirm
            :title="t('testPlan.featureCase.disassociateTip', { name: characterLimit(record.name) })"
            :sub-title-tip="t('testPlan.featureCase.disassociateTipContent')"
            :ok-text="t('common.confirm')"
            :loading="disassociateLoading"
            type="error"
            @confirm="(val, done) => handleDisassociateCase(record, done)"
          >
            <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']" type="text" class="!mr-0">
              {{ t('common.cancelLink') }}
            </MsButton>
          </MsPopconfirm>
        </template>
      </MsBaseTable>
    </template>
    <!-- 脑图 -->
    <div v-else class="h-[calc(100%-48px)] border-t border-[var(--color-text-n8)]">
      <MsTestPlanFeatureCaseMinder
        ref="msTestPlanFeatureCaseMinderRef"
        :active-module="props.activeModule"
        :module-tree="moduleTree"
        :tree-type="props.treeType"
        :plan-id="props.planId"
        :can-edit="props.canEdit"
        :table-sorter="tableSorter"
        @operation="handleMinderOperation"
        @refresh-plan="emit('refresh')"
      />
    </div>
    <!-- 批量执行 -->
    <a-modal
      v-model:visible="batchExecuteModalVisible"
      title-align="start"
      body-class="p-0"
      :width="800"
      :cancel-button-props="{ disabled: batchLoading }"
      :ok-loading="batchLoading"
      :ok-text="t('caseManagement.caseReview.commitResult')"
      @before-ok="handleBatchExecute"
      @close="resetBatchForm"
    >
      <template #title>
        {{ t('testPlan.testPlanIndex.batchExecution') }}
        <div class="text-[var(--color-text-4)]">
          {{
            t('common.selectedCount', {
              count: batchParams.currentSelectCount || tableSelected.length,
            })
          }}
        </div>
      </template>
      <ExecuteForm v-model:form="batchExecuteForm" />
    </a-modal>
    <!-- 批量修改执行人 -->
    <BatchUpdateExecutorModal
      v-model:visible="batchUpdateExecutorModalVisible"
      :count="batchParams.currentSelectCount || tableSelected.length"
      :params="batchUpdateParams"
      :show-title-count="showType === 'list'"
      :batch-update-executor="batchUpdateCaseExecutor"
      @load-list="resetCaseList"
    />
    <!-- 批量移动 -->
    <BatchApiMoveModal
      v-model:visible="batchMoveModalVisible"
      :module-tree="moduleTree"
      :count="batchParams.currentSelectCount || tableSelected.length"
      :params="batchUpdateParams"
      :batch-move="batchMoveFeatureCase"
      @load-list="resetCaseList"
    />
    <AddDefectDrawer
      v-model:visible="showCreateBugDrawer"
      :extra-params="getBugParams"
      :is-batch="isBatchAssociateOrCreate"
      :case-type="CaseLinkEnum.FUNCTIONAL"
      :fill-config="{
        isQuickFillContent: !isBatchAssociateOrCreate,
        detailId: testPlanCaseId,
        name: caseTitle,
      }"
      @success="refreshList()"
    />
    <LinkDefectDrawer
      v-model:visible="showLinkBugDrawer"
      :case-id="testPlanCaseId"
      :drawer-loading="drawerLoading"
      :load-api="AssociatedBugApiTypeEnum.TEST_PLAN_BUG_LIST"
      :show-selector-all="false"
      :is-batch="isBatchAssociateOrCreate"
      @save="saveFunctionBugHandler"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import { getFilterCustomFields, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type {
    BatchActionParams,
    BatchActionQueryParams,
    MsTableColumn,
    MsTableProps,
  } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBugOperation from '@/components/business/ms-bug-operation/index.vue';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import { getMinderOperationParams } from '@/components/business/ms-minders/caseReviewMinder/utils';
  import MsTestPlanFeatureCaseMinder from '@/components/business/ms-minders/testPlanFeatureCaseMinder/index.vue';
  import AddDefectDrawer from '@/views/case-management/components/addDefectDrawer/index.vue';
  import LinkDefectDrawer from '@/views/case-management/components/linkDefectDrawer.vue';
  import BatchApiMoveModal from '@/views/test-plan/testPlan/components/batchApiMoveModal.vue';
  import BatchUpdateExecutorModal from '@/views/test-plan/testPlan/components/batchUpdateExecutorModal.vue';
  import ExecuteForm from '@/views/test-plan/testPlan/detail/featureCase/components/executeForm.vue';

  import { getAssociatedProjectOptions, getCustomFieldsTable } from '@/api/modules/case-management/featureCase';
  import {
    associateBugToPlan,
    batchAssociatedBugToCase,
    batchDisassociateCase,
    batchExecuteCase,
    batchMoveFeatureCase,
    batchUpdateCaseExecutor,
    disassociateCase,
    getFeatureCaseModule,
    getPlanDetailFeatureCaseList,
    runFeatureCase,
    sortFeatureCase,
  } from '@/api/modules/test-plan/testPlan';
  import { defaultExecuteForm } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useTestPlanFeatureCaseStore from '@/store/modules/testPlan/testPlanFeatureCase';
  import { characterLimit } from '@/utils';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';

  import { DragSortParams, ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type { ProjectListItem } from '@/models/setting/project';
  import type { ExecuteFeatureCaseFormParams, PlanDetailFeatureCaseItem } from '@/models/testPlan/testPlan';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { AssociatedBugApiTypeEnum } from '@/enums/associateBugEnum';
  import { CaseLinkEnum, LastExecuteResults } from '@/enums/caseEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';
  import {
    executionResultMap,
    getCaseLevels,
    statusIconMap,
  } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    moduleParentId: string;
    activeModule: string;
    offspringIds: string[];
    planId: string;
    canEdit: boolean;
    treeType: 'MODULE' | 'COLLECTION';
  }>();

  const emit = defineEmits<{
    (e: 'refresh'): void;
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
    (e: 'selectParentNode', tree: ModuleTreeNode[]): void;
  }>();

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const tableStore = useTableStore();
  const { openModal } = useModal();
  const testPlanFeatureCaseStore = useTestPlanFeatureCaseStore();

  const moduleTree = computed(() => unref(testPlanFeatureCaseStore.moduleTree));

  async function initModules() {
    await testPlanFeatureCaseStore.initModules(route.query.id as string, props.treeType);
  }

  const showType = ref<'list' | 'minder'>('list');
  const keyword = ref('');

  const hasOperationPermission = computed(
    () => hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN:READ+ASSOCIATION']) && props.canEdit
  );
  const isActivated = inject<Ref<boolean>>('isActivated', ref(false));

  const columns = computed<MsTableColumn>(() => [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 100,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      columnSelectorDisabled: true,
    },
    {
      title: 'ms.minders.testSet',
      dataIndex: 'testPlanCollectionName',
      width: 150,
      showTooltip: true,
      showDrag: true,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'caseLevel',
      slotName: 'caseLevel',
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      showDrag: true,
      isTag: true,
      isStringTag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnUpdateTime',
      slotName: 'updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'lastExecResult',
      slotName: 'lastExecResult',
      filterConfig: {
        valueKey: 'key',
        labelKey: 'statusText',
        options: Object.values(executionResultMap),
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'common.belongProject',
      dataIndex: 'projectName',
      showTooltip: true,
      showInTable: false,
      showDrag: true,
      width: 150,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      slotName: 'bugCount',
      width: 100,
      showDrag: true,
    },
    {
      title: 'case.tableColumnCreateUser',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUserName',
      showTooltip: true,
      width: 150,
      showDrag: true,
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.EXECUTE_USER,
      },
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 150 : 50,
    },
  ]);

  const tableProps = ref<Partial<MsTableProps<PlanDetailFeatureCaseItem>>>({
    scroll: { x: '100%' },
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE,
    showSetting: true,
    heightUsed: 275,
    draggable: { type: 'handle' },
    draggableCondition: true,
  });

  const {
    propsRes,
    propsEvent,
    viewId,
    advanceFilter,
    setAdvanceFilter,
    loadList,
    setLoadListParams,
    resetSelector,
    getTableQueryParams,
  } = useTable(getPlanDetailFeatureCaseList, tableProps.value, (record) => {
    return {
      ...record,
      lastExecResult: record.lastExecResult ?? LastExecuteResults.PENDING,
      caseLevel: getCaseLevels(record.customFields),
    };
  });
  const existedDefect = inject<Ref<number>>('existedDefect', ref(0));

  function getLinkAction() {
    return existedDefect.value
      ? [
          {
            label: 'caseManagement.featureCase.linkDefect',
            eventTag: 'linkDefect',
            permission: ['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_BUG:READ'],
          },
        ]
      : [];
  }

  const batchActions = computed(() => {
    return {
      baseAction: [
        {
          label: 'common.execute',
          eventTag: 'execute',
          permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
        },
        {
          label: 'testPlan.featureCase.changeExecutor',
          eventTag: 'changeExecutor',
          permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
        },
        ...(props.treeType === 'COLLECTION'
          ? [{ label: 'common.move', eventTag: 'move', permission: ['PROJECT_TEST_PLAN:READ+UPDATE'] }]
          : []),
        {
          label: 'common.cancelLink',
          eventTag: 'disassociate',
          permission: ['PROJECT_TEST_PLAN:READ+ASSOCIATION'],
        },
        ...getLinkAction(),
        {
          label: 'testPlan.featureCase.noBugDataNewBug',
          eventTag: 'newBug',
          permission: ['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_BUG:READ+ADD'],
        },
      ],
    };
  });

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  watch(
    () => hasOperationPermission.value,
    () => {
      tableRef.value?.initColumn(columns.value);
    }
  );
  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all' && !isAdvancedSearchMode.value) {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }

  const collectionId = computed(() => (props.activeModule === 'all' ? '' : props.activeModule));

  async function getTableParams(isBatch: boolean) {
    const selectModules = await getModuleIds();
    const commonParams = {
      testPlanId: props.planId,
      ...(props.treeType === 'COLLECTION' ? { collectionId: collectionId.value } : { moduleIds: selectModules }),
    };
    if (isBatch) {
      return {
        condition: {
          keyword: keyword.value,
          filter: propsRes.value.filter,
          viewId: viewId.value,
          combineSearch: advanceFilter,
        },
        projectId: props.activeModule !== 'all' && props.treeType === 'MODULE' ? props.moduleParentId : '',
        ...commonParams,
      };
    }
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
      treeType: props.treeType,
      ...commonParams,
    };
  }

  watch(
    [() => props.canEdit, () => props.treeType, () => collectionId.value.length],
    () => {
      tableProps.value.draggableCondition =
        hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) &&
        props.canEdit &&
        props.treeType === 'COLLECTION' &&
        !!collectionId.value.length;
    },
    {
      immediate: true,
    }
  );

  async function loadCaseList(refreshTreeCount = true) {
    const tableParams = await getTableParams(false);
    setLoadListParams({
      ...tableParams,
      viewId: viewId.value,
      combineSearch: advanceFilter,
      projectId: props.activeModule !== 'all' && props.treeType === 'MODULE' ? props.moduleParentId : '',
    });
    resetSelector();
    loadList();
    if (refreshTreeCount && !isAdvancedSearchMode.value) {
      testPlanFeatureCaseStore.getModuleCount({
        ...tableParams,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
      });
    }
  }

  const projectList = ref<ProjectListItem[]>([]);
  async function initProjectList() {
    try {
      projectList.value = await getAssociatedProjectOptions(appStore.currentOrgId, CaseLinkEnum.SCENARIO);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const anotherTree = ref<ModuleTreeNode[]>([]);
  async function initAnotherModules() {
    try {
      const res = await getFeatureCaseModule({
        testPlanId: props.planId,
        treeType: props.treeType === 'MODULE' ? 'COLLECTION' : 'MODULE',
      });
      anotherTree.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  watch(
    () => props.treeType,
    () => {
      initAnotherModules();
    }
  );

  const reviewResultOptions = computed(() => {
    return Object.keys(statusIconMap).map((key) => {
      return {
        value: key,
        label: statusIconMap[key].statusText,
      };
    });
  });

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      type: FilterType.INPUT,
    },
    {
      title: 'ms.minders.testSet',
      dataIndex: 'testPlanCollectionId',
      type: FilterType.SELECT,
      selectProps: {
        labelKey: 'name',
        valueKey: 'id',
        multiple: true,
        options: props.treeType !== 'MODULE' ? moduleTree.value : anotherTree.value,
      },
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      type: FilterType.TREE_SELECT,
      treeSelectData: (props.treeType === 'MODULE' ? moduleTree.value : anotherTree.value).map((node) => {
        return {
          ...node,
          disabled: true, // 项目层级的禁用掉
        };
      }),
      treeSelectProps: {
        fieldNames: {
          title: 'name',
          key: 'id',
          children: 'children',
        },
        multiple: true,
        treeCheckable: true,
        treeCheckStrictly: true,
      },
    },
    {
      title: 'common.belongProject',
      dataIndex: 'projectName',
      type: FilterType.SELECT,
      selectProps: {
        labelKey: 'name',
        valueKey: 'id',
        multiple: true,
        options: projectList.value,
      },
    },
    {
      title: 'caseManagement.featureCase.tableColumnReviewResult',
      dataIndex: 'reviewStatus',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: reviewResultOptions.value,
      },
    },
    {
      title: 'common.executionResult',
      dataIndex: 'lastExecResult',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        valueKey: 'key',
        labelKey: 'statusText',
        options: Object.values(executionResultMap),
      },
    },
    {
      title: 'caseManagement.featureCase.associatedDemand',
      dataIndex: 'demand',
      type: FilterType.INPUT,
    },
    {
      title: 'caseManagement.featureCase.relatedAttachments',
      dataIndex: 'attachment',
      type: FilterType.INPUT,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      type: FilterType.TAGS_INPUT,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      type: FilterType.NUMBER,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.creator',
      dataIndex: 'createUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
    {
      title: 'apiScenario.table.columns.updateUser',
      dataIndex: 'updateUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      type: FilterType.DATE_PICKER,
    },
  ]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
  async function initFilter() {
    try {
      const result = await getCustomFieldsTable(appStore.currentProjectId);
      searchCustomFields.value = getFilterCustomFields(result); // 处理自定义字段
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string, isStartAdvance: boolean) => {
    resetSelector();
    emit('handleAdvSearch', isStartAdvance);
    keyword.value = '';
    setAdvanceFilter(filter, id);
    await loadCaseList(); // 基础筛选都清空
  };

  watch(
    () => props.activeModule,
    () => {
      if (showType.value === 'list' && !isAdvancedSearchMode.value) {
        loadCaseList();
      }
    }
  );

  onBeforeMount(async () => {
    await initFilter();
    loadCaseList();
    initProjectList();
    initAnotherModules();
  });

  onActivated(() => {
    if (isActivated.value) {
      loadCaseList();
    }
  });

  const modulesCount = computed(() => testPlanFeatureCaseStore.modulesCount);

  async function getModuleCount() {
    let params;
    const tableParams = await getTableParams(false);
    if (showType.value === 'list') {
      params = {
        ...tableParams,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
      };
    } else {
      params = { treeType: props.treeType, moduleIds: [], testPlanId: props.planId, pageSize: 10, current: 1 };
    }
    await testPlanFeatureCaseStore.getModuleCount(params);
  }

  const tableSorter = ref<Record<string, string>>({});
  function handleSorterChange(sorter: { [key: string]: string }) {
    tableSorter.value = sorter;
  }

  /**
   * 更新数据
   * @param getCount 获取模块树数量
   */
  const msTestPlanFeatureCaseMinderRef = ref<InstanceType<typeof MsTestPlanFeatureCaseMinder>>();

  async function refresh(getCount = true) {
    if (showType.value === 'list') {
      loadCaseList(getCount);
    } else {
      if (getCount) {
        await getModuleCount();
      }
      msTestPlanFeatureCaseMinderRef.value?.initCaseTree();
    }
  }

  async function handleRefreshAll() {
    emit('refresh');
    await initModules();
    refresh();
  }

  function handleShowTypeChange(val: string | number | boolean) {
    if (val === 'minder') {
      getModuleCount(); // 切换到脑图刷新模块统计
    } else {
      loadCaseList();
    }
  }

  const tableSelected = ref<(string | number)[]>([]); // 表格选中的
  const batchParams = ref<BatchActionQueryParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [],
    condition: {},
    currentSelectCount: 0,
  });

  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  function resetCaseList() {
    if (showType.value === 'list') {
      resetSelector();
      getModuleCount();
      loadList();
    }
  }

  // 拖拽排序
  async function handleDragChange(params: DragSortParams) {
    try {
      await sortFeatureCase({ ...params, testCollectionId: collectionId.value });
      Message.success(t('caseManagement.featureCase.sortSuccess'));
      loadCaseList(false);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 更新执行结果
  async function handleEditLastExecResult(record: PlanDetailFeatureCaseItem) {
    try {
      await runFeatureCase({
        id: record.id,
        projectId: appStore.currentProjectId,
        testPlanId: props.planId,
        caseId: record.caseId,
        lastExecResult: record.lastExecResult,
      });
      Message.success(t('common.updateSuccess'));
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 取消关联
  const disassociateLoading = ref(false);

  async function handleDisassociateCase(record: PlanDetailFeatureCaseItem, done?: () => void) {
    try {
      disassociateLoading.value = true;
      await disassociateCase({ testPlanId: props.planId, id: record.id });
      if (done) {
        done();
      }
      Message.success(t('common.unLinkSuccess'));
      resetCaseList();
      initModules();
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      disassociateLoading.value = false;
    }
  }

  const batchUpdateParams = ref();
  const minderSelectData = ref<MinderJsonNodeData>(); // 当前脑图选中的数据

  // 批量取消关联用例
  function handleBatchDisassociateCase() {
    const count =
      showType.value !== 'list'
        ? minderSelectData.value?.count
        : batchParams.value.currentSelectCount || tableSelected.value.length;
    const batchDisassociateTitle =
      showType.value !== 'list' && minderSelectData.value?.caseId?.length
        ? t('testPlan.featureCase.disassociateTip', { name: characterLimit(minderSelectData.value?.text) })
        : t('caseManagement.caseReview.disassociateConfirmTitle', { count });
    openModal({
      type: 'warning',
      title: batchDisassociateTitle,
      content: t('testPlan.featureCase.batchDisassociateTipContent'),
      okText: t('common.cancelLink'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          await batchDisassociateCase(batchUpdateParams.value);
          Message.success(t('common.updateSuccess'));
          const tree = cloneDeep(moduleTree.value);
          emit('refresh');
          await initModules();
          await getModuleCount();
          if (!Object.keys(modulesCount.value).includes(props.activeModule)) {
            // 模块树选中返回上一级
            emit('selectParentNode', tree);
          } else {
            refresh(false);
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 批量执行
  const batchLoading = ref(false);
  const batchExecuteModalVisible = ref(false);
  const batchExecuteForm = ref<ExecuteFeatureCaseFormParams>({ ...defaultExecuteForm });
  async function handleBatchExecute() {
    try {
      batchLoading.value = true;
      const tableParams = await getTableParams(true);
      await batchExecuteCase({
        ...tableParams,
        ...batchExecuteForm.value,
        notifier: batchExecuteForm.value?.commentIds?.join(';'),
        selectIds: tableSelected.value as string[],
        selectAll: batchParams.value.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
      });
      Message.success(t('common.updateSuccess'));
      resetCaseList();
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchLoading.value = false;
    }
  }

  function resetBatchForm() {
    batchExecuteForm.value = { ...defaultExecuteForm };
  }

  const showLinkBugDrawer = ref<boolean>(false);
  const associatedCaseId = ref<string>('');
  const testPlanCaseId = ref<string>('');
  const drawerLoading = ref<boolean>(false);
  const isBatchAssociateOrCreate = ref(false);

  async function getBugParams() {
    if (isBatchAssociateOrCreate.value) {
      const tableParams = await getTableParams(true);
      return {
        ...tableParams,
        projectId: appStore.currentProjectId,
        selectIds: tableSelected.value as string[],
        selectAll: batchParams.value.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        testPlanId: props.planId,
      };
    }
    return { caseId: associatedCaseId.value, testPlanId: props.planId, testPlanCaseId: testPlanCaseId.value };
  }

  // 功能用例关联缺陷
  async function saveFunctionBugHandler(params: TableQueryParams) {
    try {
      drawerLoading.value = true;
      const tableParams = await getTableParams(true);
      if (isBatchAssociateOrCreate.value) {
        await batchAssociatedBugToCase({
          selectIds: tableSelected.value as string[],
          selectAll: batchParams.value.selectAll,
          excludeIds: batchParams.value?.excludeIds || [],
          ...tableParams,
          projectId: appStore.currentProjectId,
          bugIds: params.selectIds,
        });
      } else {
        await associateBugToPlan({
          ...params,
          projectId: appStore.currentProjectId,
          caseId: associatedCaseId.value,
          testPlanId: props.planId,
          testPlanCaseId: testPlanCaseId.value,
        });
      }

      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      showLinkBugDrawer.value = false;
      resetCaseList();
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  function refreshList() {
    resetCaseList();
    emit('refresh');
  }

  const showCreateBugDrawer = ref<boolean>(false);
  const caseTitle = ref<string>('');

  // 关联/关联缺陷
  function associateAndCreateDefect(isAssociate: boolean, isBatch: boolean, record?: PlanDetailFeatureCaseItem) {
    isBatchAssociateOrCreate.value = isBatch;
    if (record) {
      const { id, caseId, name, lastExecResult } = record;
      associatedCaseId.value = caseId;
      testPlanCaseId.value = id;
      let firstName = name;
      const lastStatusName =
        LastExecuteResults.PENDING === lastExecResult
          ? ''
          : `_${t(executionResultMap[lastExecResult]?.statusText ?? '')}`;
      caseTitle.value = `${firstName}${lastStatusName}`;
      if (caseTitle.value.length > 255) {
        firstName = firstName.slice(0, 251);
        caseTitle.value = `${firstName}${lastStatusName}`;
      }
    }
    if (isAssociate) {
      showLinkBugDrawer.value = true;
    } else {
      showCreateBugDrawer.value = true;
    }
  }

  // 批量修改执行人 和 批量移动
  const batchUpdateExecutorModalVisible = ref(false);
  const batchMoveModalVisible = ref(false);

  function handleOperation(type?: string) {
    switch (type) {
      case 'execute':
        batchExecuteModalVisible.value = true;
        break;
      case 'disassociate':
        handleBatchDisassociateCase();
        break;
      case 'changeExecutor':
        batchUpdateExecutorModalVisible.value = true;
        break;
      case 'move':
        batchMoveModalVisible.value = true;
        break;
      case 'linkDefect':
        associateAndCreateDefect(true, true);
        break;
      case 'newBug':
        associateAndCreateDefect(false, true);
        break;
      default:
        break;
    }
  }

  // 脑图操作
  function handleMinderOperation(type: string, node: MinderJsonNode) {
    minderSelectData.value = node.data;
    batchUpdateParams.value = {
      ...getMinderOperationParams(node, props.treeType === 'COLLECTION'),
      testPlanId: props.planId,
      projectId: node.data?.id !== 'NONE' ? node.data?.projectId : '',
    };
    handleOperation(type);
  }

  // 处理表格选中后批量操作
  async function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = { ...params, selectIds: params?.selectedIds };
    const tableParams = await getTableParams(true);
    batchUpdateParams.value = {
      selectIds: tableSelected.value as string[],
      selectAll: batchParams.value.selectAll,
      excludeIds: batchParams.value?.excludeIds || [],
      ...tableParams,
    };
    handleOperation(event.eventTag);
  }

  // 去用例详情页面
  function toCaseDetail(record: PlanDetailFeatureCaseItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: record.caseId,
        testPlanCaseId: record.id,
        canEdit: String(props.canEdit),
      },
      state: {
        params: JSON.stringify(getTableQueryParams()),
      },
    });
  }

  defineExpose({
    resetSelector,
    getModuleCount,
    refresh,
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE, columns.value, 'drawer', true);
</script>

<style lang="less" scoped>
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
  .list-show-type {
    padding: 0;
    :deep(.arco-radio-button-content) {
      padding: 4px 6px;
    }
  }
</style>
