<template>
  <div class="p-[16px]">
    <MsAdvanceFilter
      v-model:keyword="keyword"
      :filter-config-list="[]"
      :custom-fields-config-list="[]"
      :row-count="0"
      :count="props.modulesCount[props.activeModule] || 0"
      :name="moduleNamePath"
      :search-placeholder="t('common.searchByIdName')"
      @keyword-search="loadCaseList()"
      @adv-search="loadCaseList()"
      @refresh="loadCaseList()"
    />
    <a-spin :loading="tableLoading" class="w-full">
      <MsBaseTable
        ref="tableRef"
        class="mt-[16px]"
        v-bind="propsRes"
        :action-config="batchActions"
        v-on="propsEvent"
        @batch-action="handleTableBatch"
        @drag-change="handleDragChange"
        @selected-change="handleTableSelect"
        @filter-change="getModuleCount"
        @module-change="loadCaseList(false)"
      >
        <template #num="{ record }">
          <MsButton type="text" @click="toDetail(record)">{{ record.num }}</MsButton>
        </template>
        <template #protocol="{ record }">
          <ApiMethodName :method="record.protocol" />
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
            @click="showReport(record)"
          />
        </template>
        <template #status="{ record }">
          <apiStatus :status="record.status" />
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
          <a-divider v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']" direction="vertical" :margin="8"></a-divider>
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
      :report-detail="getApiCaseReport"
      :get-report-step-detail="getApiCaseReportStep"
    />
    <!-- 批量移动 -->
    <BatchApiMoveModal
      v-model:visible="batchMoveModalVisible"
      :module-tree="props.moduleTree"
      :count="batchParams.currentSelectCount || tableSelected.length"
      :params="batchUpdateParams"
      :batch-move="batchMoveApiCase"
      @load-list="resetCaseList"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
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
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ApiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import CaseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import BatchApiMoveModal from '@/views/test-plan/testPlan/components/batchApiMoveModal.vue';

  import {
    batchDisassociateApiCase,
    batchMoveApiCase,
    batchRunApiCase,
    disassociateApiCase,
    getApiCaseReport,
    getApiCaseReportStep,
    getPlanDetailApiCaseList,
    runApiCase,
    sortApiCase,
  } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useTableStore from '@/hooks/useTableStore';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { DragSortParams, ModuleTreeNode } from '@/models/common';
  import type { PlanDetailApiCaseItem, PlanDetailApiCaseQueryParams } from '@/models/testPlan/testPlan';
  import { ReportEnum } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    modulesCount: Record<string, number>; // 模块数量统计对象
    moduleName: string;
    moduleParentId: string;
    activeModule: string;
    offspringIds: string[];
    planId: string;
    moduleTree: ModuleTreeNode[];
    canEdit: boolean;
    selectedProtocols: string[];
    treeType: 'MODULE' | 'COLLECTION';
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: PlanDetailApiCaseQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
  }>();

  const { t } = useI18n();
  const tableStore = useTableStore();
  const { openModal } = useModal();
  const { openNewPage } = useOpenNewPage();

  const keyword = ref('');
  const moduleNamePath = computed(() => {
    return props.activeModule === 'all' ? t('apiTestManagement.allApi') : props.moduleName;
  });

  const hasOperationPermission = computed(
    () => hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN:READ+ASSOCIATION']) && props.canEdit
  );
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
      fixed: 'left',
      width: 150,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'case.caseName',
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
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      slotName: 'protocol',
      width: 150,
      showDrag: true,
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
      width: 150,
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
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 200,
      showDrag: true,
      showInTable: false,
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
      showDrag: true,
      width: 150,
    },
    {
      title: 'report.detail.api.executeEnv',
      dataIndex: 'environmentName',
      width: 150,
      showTooltip: true,
      showInTable: false,
      showDrag: true,
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
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 200 : 50,
    },
  ]);

  const tableProps = ref<Partial<MsTableProps<PlanDetailApiCaseItem>>>({
    scroll: { x: '100%' },
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_API_CASE,
    showSetting: true,
    heightUsed: 460,
    showSubdirectory: true,
    draggable: { type: 'handle' },
    draggableCondition: true,
    selectable: hasOperationPermission.value,
  });

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getPlanDetailApiCaseList,
    tableProps.value
  );

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  watch(
    () => hasOperationPermission.value,
    () => {
      tableRef.value?.initColumn(columns.value);
    }
  );

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
      ],
    };
  });

  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all') {
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
      protocols: props.selectedProtocols,
      ...(props.treeType === 'COLLECTION' ? { collectionId: collectionId.value } : { moduleIds: selectModules }),
    };
    if (isBatch) {
      return {
        condition: {
          keyword: keyword.value,
          filter: propsRes.value.filter,
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
      projectId: props.activeModule !== 'all' && props.treeType === 'MODULE' ? props.moduleParentId : '',
    });
    loadList();
    if (refreshTreeCount) {
      emit('getModuleCount', {
        ...tableParams,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
      });
    }
  }
  watch([() => props.activeModule, () => props.selectedProtocols], () => {
    loadCaseList();
  });

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
  function showReport(record: PlanDetailApiCaseItem) {
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
      await sortApiCase({ ...params, testCollectionId: collectionId.value });
      Message.success(t('caseManagement.featureCase.sortSuccess'));
      loadCaseList(false);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 取消关联
  const disassociateLoading = ref(false);
  async function handleDisassociateCase(record: PlanDetailApiCaseItem, done?: () => void) {
    try {
      disassociateLoading.value = true;
      await disassociateApiCase({ testPlanId: props.planId, id: record.id });
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
  async function handleRun(record: PlanDetailApiCaseItem) {
    try {
      tableLoading.value = true;
      await runApiCase(record.id);
      Message.success(t('common.executionSuccess'));
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
      await batchRunApiCase({
        selectIds: tableSelected.value as string[],
        selectAll: batchParams.value.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        ...tableParams,
      });
      Message.success(t('common.executionSuccess'));
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
          await batchDisassociateApiCase({
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
      default:
        break;
    }
  }

  // 去接口用例详情页面
  function toDetail(record: PlanDetailApiCaseItem) {
    openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
      cId: record.apiTestCaseId,
      pId: record.projectId,
    });
  }

  defineExpose({
    resetSelector,
    loadCaseList,
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_DETAIL_API_CASE, columns.value, 'drawer', true);
</script>
