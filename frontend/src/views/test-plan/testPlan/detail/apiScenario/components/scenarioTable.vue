<template>
  <div class="p-[16px]">
    <MsAdvanceFilter
      ref="msAdvanceFilterRef"
      v-model:keyword="keyword"
      :view-type="ViewTypeEnum.PLAN_API_SCENARIO"
      :filter-config-list="filterConfigList"
      :search-placeholder="t('common.searchByIdName')"
      @keyword-search="loadCaseList()"
      @adv-search="handleAdvSearch"
      @refresh="handleRefreshAll()"
    />
    <a-spin :loading="tableLoading" class="w-full">
      <MsBaseTable
        ref="tableRef"
        class="mt-[16px]"
        v-bind="propsRes"
        :action-config="batchActions"
        :not-show-table-filter="isAdvancedSearchMode"
        v-on="propsEvent"
        @batch-action="handleTableBatch"
        @drag-change="handleDragChange"
        @selected-change="handleTableSelect"
        @filter-change="getModuleCount"
      >
        <template #num="{ record }">
          <MsButton type="text" @click="toDetail(record)">{{ record.num }}</MsButton>
        </template>
        <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
          <CaseLevel :case-level="filterContent.value" />
        </template>
        <template #caseLevel="{ record }">
          <CaseLevel :case-level="record.priority" />
        </template>
        <template #[FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS]="{ filterContent }">
          <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
        </template>
        <template #lastExecResult="{ record }">
          <ExecutionStatus
            :module-type="ReportEnum.API_REPORT"
            :status="record.lastExecResult"
            :class="[!record.lastExecReportId ? '' : 'cursor-pointer']"
            :script-identifier="record.scriptIdentifier"
            @click="showReport(record)"
          />
        </template>
        <template #status="{ record }">
          <apiStatus :status="record.status" />
        </template>
        <template #bugCount="{ record }">
          <MsBugOperation
            :case-type="CaseLinkEnum.SCENARIO"
            :can-edit="props.canEdit"
            :bug-list="record.bugList"
            :resource-id="record.id"
            :bug-count="record.bugCount || 0"
            :existed-defect="existedDefect"
            :permission="['PROJECT_TEST_PLAN:READ+EXECUTE']"
            @load-list="refreshListAndDetail()"
            @associated="associateAndCreateDefect(true, false, record)"
            @create="associateAndCreateDefect(false, false, record)"
          />
        </template>
        <template v-if="props.canEdit" #operation="{ record }">
          <MsButton
            v-permission="['PROJECT_TEST_PLAN:READ+EXECUTE']"
            type="text"
            class="!mr-0"
            @click="handleRun(record)"
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
    </a-spin>
    <CaseAndScenarioReportDrawer
      v-model:visible="reportVisible"
      :report-id="reportId"
      do-not-show-share
      is-scenario
      :report-detail="getApiScenarioReport"
      :get-report-step-detail="getApiScenarioReportStep"
    />
    <!-- 批量移动 -->
    <BatchApiMoveModal
      v-model:visible="batchMoveModalVisible"
      :module-tree="props.moduleTree"
      :count="batchParams.currentSelectCount || tableSelected.length"
      :params="batchUpdateParams"
      :batch-move="batchMoveApiScenario"
      @load-list="resetCaseList"
    />

    <AddDefectDrawer
      v-model:visible="showCreateBugDrawer"
      :extra-params="getScenarioBugParams"
      :is-batch="isBatchAssociateOrCreate"
      :case-type="CaseLinkEnum.SCENARIO"
      :fill-config="{
        isQuickFillContent: !isBatchAssociateOrCreate,
        detailId: lastExecuteReportId,
        name: caseTitle,
      }"
      @success="refreshListAndDetail()"
    />
    <LinkDefectDrawer
      v-model:visible="showLinkBugDrawer"
      :case-id="testPlanCaseId"
      :load-api="AssociatedBugApiTypeEnum.SCENARIO_BUG_LIST"
      :is-batch="isBatchAssociateOrCreate"
      :drawer-loading="drawerLoading"
      :show-selector-all="false"
      @save="saveScenarioBugHandler"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsAdvanceFilter from '@/components/pure/ms-advance-filter/index.vue';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
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
  import { lastExecuteResultMap } from '@/components/business/ms-case-associate/utils';
  import MsRichMessage from '@/components/business/ms-rich-message/index.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import CaseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import AddDefectDrawer from '@/views/case-management/components/addDefectDrawer/index.vue';
  import LinkDefectDrawer from '@/views/case-management/components/linkDefectDrawer.vue';
  import BatchApiMoveModal from '@/views/test-plan/testPlan/components/batchApiMoveModal.vue';

  import { getAssociatedProjectOptions } from '@/api/modules/case-management/featureCase';
  import {
    associateBugToScenarioCase,
    batchDisassociateApiScenario,
    batchLinkBugToScenarioCase,
    batchMoveApiScenario,
    batchRunApiScenario,
    disassociateApiScenario,
    getApiScenarioModule,
    getApiScenarioReport,
    getApiScenarioReportStep,
    getPlanDetailApiScenarioList,
    runApiScenario,
    sortApiScenario,
  } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useGlobalStore from '@/store/modules/global';
  import { characterLimit, getGenerateId } from '@/utils';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';

  import { DragSortParams, ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type { ProjectListItem } from '@/models/setting/project';
  import type { PlanDetailApiScenarioItem, PlanDetailApiScenarioQueryParams } from '@/models/testPlan/testPlan';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { AssociatedBugApiTypeEnum } from '@/enums/associateBugEnum';
  import { CaseLinkEnum, LastExecuteResults } from '@/enums/caseEnum';
  import { GlobalEventNameEnum } from '@/enums/commonEnum';
  import { ReportEnum } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { TaskCenterEnum } from '@/enums/taskCenter';

  import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';
  import { scenarioStatusOptions } from '@/views/api-test/scenario/components/config';

  const props = defineProps<{
    moduleParentId: string;
    activeModule: string;
    offspringIds: string[];
    planId: string;
    moduleTree: ModuleTreeNode[];
    canEdit: boolean;
    treeType: 'MODULE' | 'COLLECTION';
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: PlanDetailApiScenarioQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
  }>();

  const { t } = useI18n();
  const tableStore = useTableStore();
  const { openModal } = useModal();
  const { openNewPage } = useOpenNewPage();
  const appStore = useAppStore();
  const globalStore = useGlobalStore();

  const keyword = ref('');

  const hasOperationPermission = computed(
    () => hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN:READ+ASSOCIATION']) && props.canEdit
  );
  const columns = computed<MsTableColumn>(() => [
    {
      title: 'ID',
      slotName: 'num',
      dataIndex: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 150,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiScenario.table.columns.name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 150,
      showTooltip: true,
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
      title: 'apiScenario.table.columns.level',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'lastExecResult',
      slotName: 'lastExecResult',
      filterConfig: {
        options: lastReportStatusListOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      width: 150,
      showDrag: true,
      showInTable: false,
    },
    {
      title: 'apiScenario.table.columns.createTime',
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
      title: 'apiScenario.table.columns.updateTime',
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
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      slotName: 'bugCount',
      width: 100,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'common.belongProject',
      dataIndex: 'projectName',
      showTooltip: true,
      showDrag: true,
      width: 150,
    },
    {
      title: 'report.detail.api.executeEnv',
      dataIndex: 'environmentName',
      width: 150,
      showDrag: true,
      showTooltip: true,
      showInTable: false,
    },
    {
      title: 'case.tableColumnCreateUser',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 130,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUserName',
      showTooltip: true,
      width: 130,
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

  const tableProps = ref<Partial<MsTableProps<PlanDetailApiScenarioItem>>>({
    scroll: { x: '100%' },
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_API_CASE,
    showSetting: true,
    heightUsed: 275,
    draggable: { type: 'handle' },
    draggableCondition: true,
    selectable: hasOperationPermission.value,
  });

  const { propsRes, propsEvent, viewId, advanceFilter, setAdvanceFilter, loadList, setLoadListParams, resetSelector } =
    useTable(getPlanDetailApiScenarioList, tableProps.value);

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  watch(
    () => hasOperationPermission.value,
    () => {
      tableRef.value?.initColumn(columns.value);
    }
  );
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

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all' && !isAdvancedSearchMode.value) {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.TEST_PLAN_DETAIL_API_CASE);
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
      treeType: props.treeType,
      keyword: keyword.value,
      filter: propsRes.value.filter,
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
    loadList();
    if (refreshTreeCount && !isAdvancedSearchMode.value) {
      emit('getModuleCount', {
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
      const res = await getApiScenarioModule({
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

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'apiScenario.table.columns.name',
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
        options: props.treeType !== 'MODULE' ? props.moduleTree : anotherTree.value,
      },
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      type: FilterType.TREE_SELECT,
      treeSelectData: (props.treeType === 'MODULE' ? props.moduleTree : anotherTree.value).map((node) => {
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
      title: 'apiScenario.table.columns.level',
      dataIndex: 'priority',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: casePriorityOptions,
      },
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: scenarioStatusOptions,
      },
    },
    {
      title: 'common.executionResult',
      dataIndex: 'lastExecResult',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: lastReportStatusListOptions.value,
      },
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
      title: 'report.detail.api.executeEnv',
      dataIndex: 'environmentName',
      type: FilterType.SELECT,
      selectProps: {
        labelKey: 'name',
        valueKey: 'id',
        multiple: true,
        options: appStore.envList,
      },
    },
    {
      title: 'apiScenario.table.columns.steps',
      dataIndex: 'stepTotal',
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
      if (isAdvancedSearchMode.value) return;
      loadCaseList();
    }
  );

  async function handleRefreshAll() {
    emit('refresh');
    emit('initModules');
    loadCaseList();
  }

  function refreshListAndDetail() {
    resetSelector();
    loadCaseList();
    emit('refresh');
  }

  async function getModuleCount() {
    const tableParams = await getTableParams(false);
    emit('getModuleCount', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }

  // 显示执行报告
  const reportVisible = ref(false);
  const reportId = ref('');

  function showReport(record: PlanDetailApiScenarioItem) {
    if (!record.lastExecReportId) return;
    reportVisible.value = true;
    reportId.value = record.lastExecReportId;
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
    resetSelector();
    getModuleCount();
    loadList();
  }

  function resetSelectorAndCaseList() {
    resetSelector();
    loadList();
  }

  // 拖拽排序
  async function handleDragChange(params: DragSortParams) {
    try {
      await sortApiScenario({ ...params, testCollectionId: collectionId.value });
      Message.success(t('caseManagement.featureCase.sortSuccess'));
      loadCaseList(false);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 取消关联
  const disassociateLoading = ref(false);

  async function handleDisassociateCase(record: PlanDetailApiScenarioItem, done?: () => void) {
    try {
      disassociateLoading.value = true;
      await disassociateApiScenario({ testPlanId: props.planId, id: record.id });
      if (done) {
        done();
      }
      Message.success(t('common.unLinkSuccess'));
      resetCaseList();
      emit('initModules');
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      disassociateLoading.value = false;
    }
  }

  // 执行
  const tableLoading = ref(false); // 包含批量操作按钮，防止重复发起请求
  async function handleRun(record: PlanDetailApiScenarioItem) {
    try {
      tableLoading.value = true;
      await runApiScenario(record.id);
      Message.success({
        content: () =>
          h(
            'div',
            {
              style: {
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
              },
            },
            [
              h(MsRichMessage, {
                content: t('case.detail.execute.success'),
                onGoDetail() {
                  globalStore.dispatchGlobalEvent({
                    id: getGenerateId(),
                    name: GlobalEventNameEnum.OPEN_TASK_CENTER,
                    params: {
                      tab: TaskCenterEnum.DETAIL,
                    },
                  });
                },
              }),
            ]
          ),
        duration: 5000,
        closable: true,
      });
      resetSelectorAndCaseList();
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      tableLoading.value = false;
    }
  }

  // 批量执行
  async function handleBatchRun() {
    try {
      tableLoading.value = true;
      const tableParams = await getTableParams(true);
      await batchRunApiScenario({
        selectIds: tableSelected.value as string[],
        selectAll: batchParams.value.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        ...tableParams,
      });
      Message.success({
        content: () =>
          h(
            'div',
            {
              style: {
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
              },
            },
            [
              h(MsRichMessage, {
                content: t('case.detail.execute.success'),
                onGoDetail() {
                  globalStore.dispatchGlobalEvent({
                    id: getGenerateId(),
                    name: GlobalEventNameEnum.OPEN_TASK_CENTER,
                    params: {
                      tab: TaskCenterEnum.DETAIL,
                    },
                  });
                },
              }),
            ]
          ),
        duration: 5000,
        closable: true,
      });
      resetSelectorAndCaseList();
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      tableLoading.value = false;
    }
  }

  // 批量取消关联用例
  function handleBatchDisassociateCase() {
    openModal({
      type: 'warning',
      title: t('caseManagement.caseReview.disassociateConfirmTitle', {
        count: batchParams.value.currentSelectCount || tableSelected.value.length,
      }),
      content: t('testPlan.featureCase.batchDisassociateTipContent'),
      okText: t('common.cancelLink'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          const tableParams = await getTableParams(true);
          await batchDisassociateApiScenario({
            selectIds: tableSelected.value as string[],
            selectAll: batchParams.value.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            ...tableParams,
          });
          Message.success(t('common.updateSuccess'));
          resetCaseList();
          emit('initModules');
          emit('refresh');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 批量批量移动
  const batchUpdateParams = ref();
  const batchMoveModalVisible = ref(false);

  const isBatchAssociateOrCreate = ref(false);
  const showLinkBugDrawer = ref<boolean>(false);
  const associatedCaseId = ref<string>('');
  const testPlanCaseId = ref<string>('');
  const showCreateBugDrawer = ref<boolean>(false);
  const lastExecuteReportId = ref<string>('');
  const caseTitle = ref<string>('');

  const drawerLoading = ref<boolean>(false);
  // 关联缺陷
  function associateAndCreateDefect(isAssociate: boolean, isBatch: boolean, record?: PlanDetailApiScenarioItem) {
    isBatchAssociateOrCreate.value = isBatch;
    if (record) {
      const { id, apiScenarioId, lastExecReportId, name, lastExecResult } = record;
      associatedCaseId.value = apiScenarioId;
      testPlanCaseId.value = id;
      lastExecuteReportId.value = lastExecReportId;
      let firstName = name;
      const lastStatusName =
        lastExecResult === LastExecuteResults.PENDING
          ? ''
          : `_${t(lastExecuteResultMap[lastExecResult]?.statusText ?? '')}`;
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

  // 场景关联缺陷
  async function saveScenarioBugHandler(params: TableQueryParams) {
    try {
      drawerLoading.value = true;
      const tableParams = await getTableParams(true);
      if (isBatchAssociateOrCreate.value) {
        await batchLinkBugToScenarioCase({
          selectIds: tableSelected.value as string[],
          selectAll: batchParams.value.selectAll,
          excludeIds: batchParams.value?.excludeIds || [],
          ...tableParams,
          bugIds: params.selectIds,
          projectId: appStore.currentProjectId,
        });
      } else {
        await associateBugToScenarioCase({
          ...params,
          caseId: associatedCaseId.value,
          testPlanId: props.planId,
          testPlanCaseId: testPlanCaseId.value,
          projectId: appStore.currentProjectId,
        });
      }

      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      showLinkBugDrawer.value = false;
      resetSelectorAndCaseList();
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  async function getScenarioBugParams() {
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
    switch (event.eventTag) {
      case 'execute':
        handleBatchRun();
        break;
      case 'disassociate':
        handleBatchDisassociateCase();
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

  // 去接口场景详情页面
  function toDetail(record: PlanDetailApiScenarioItem) {
    openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
      id: record.apiScenarioId,
      pId: record.projectId,
    });
  }

  onBeforeMount(() => {
    loadCaseList();
    initAnotherModules();
    initProjectList();
  });

  defineExpose({
    resetSelector,
    loadCaseList,
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_DETAIL_API_CASE, columns.value, 'drawer', true);
</script>
