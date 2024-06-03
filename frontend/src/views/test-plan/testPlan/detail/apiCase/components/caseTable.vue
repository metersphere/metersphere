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
      @keyword-search="loadCaseList"
      @adv-search="loadCaseList"
      @refresh="loadCaseList"
    />
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
    >
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
        <ExecuteResult :execute-result="record.lastExecResult" />
        <MsIcon
          v-show="record.lastExecResult !== LastExecuteResults.PENDING"
          type="icon-icon_take-action_outlined"
          class="ml-[8px] cursor-pointer text-[rgb(var(--primary-5))]"
          size="16"
        />
      </template>
      <template #status="{ record }">
        <apiStatus :status="record.status" />
      </template>
      <template v-if="props.canEdit" #operation="{ record }">
        <MsButton v-permission="['PROJECT_TEST_PLAN:READ+EXECUTE']" type="text" class="!mr-0">
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
        <a-divider
          v-if="props.repeatCase"
          v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']"
          direction="vertical"
          :margin="8"
        ></a-divider>
        <MsButton
          v-if="props.repeatCase"
          v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']"
          type="text"
          class="!mr-0"
          @click="handleCopyCase(record)"
        >
          {{ t('common.copy') }}
        </MsButton>
      </template>
    </MsBaseTable>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
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
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import {
    associationCaseToPlan,
    batchDisassociateCase,
    disassociateCase,
    getPlanDetailFeatureCaseList,
    sortFeatureCase,
  } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { DragSortParams, ModuleTreeNode } from '@/models/common';
  import type { PlanDetailFeatureCaseItem, PlanDetailFeatureCaseListQueryParams } from '@/models/testPlan/testPlan';
  import { LastExecuteResults } from '@/enums/caseEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';
  import {
    executionResultMap,
    getCaseLevels,
    getModules,
  } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    modulesCount: Record<string, number>; // 模块数量统计对象
    moduleName: string;
    activeModule: string;
    offspringIds: string[];
    planId: string;
    moduleTree: ModuleTreeNode[];
    repeatCase: boolean;
    canEdit: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: PlanDetailFeatureCaseListQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();
  const tableStore = useTableStore();
  const { openModal } = useModal();

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
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 100,
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
      dataIndex: 'moduleId',
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
      dataIndex: 'executeEnv',
      width: 150,
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

  const tableProps = ref<Partial<MsTableProps<PlanDetailFeatureCaseItem>>>({
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
    // TODO 联调
    getPlanDetailFeatureCaseList,
    tableProps.value,
    (record) => {
      return {
        ...record,
        lastExecResult: record.lastExecResult ?? LastExecuteResults.PENDING,
        caseLevel: getCaseLevels(record.customFields),
        moduleId: getModules(record.moduleId, props.moduleTree),
      };
    }
  );

  watch(
    () => props.canEdit,
    (val) => {
      tableProps.value.draggableCondition = hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) && val;
    },
    {
      immediate: true,
    }
  );
  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  watch(
    () => hasOperationPermission.value,
    () => {
      tableRef.value?.initColumn(columns.value);
    }
  );

  const batchActions = {
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
      {
        label: 'common.move',
        eventTag: 'move',
        permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
      },
      {
        label: 'common.cancelLink',
        eventTag: 'disassociate',
        permission: ['PROJECT_TEST_PLAN:READ+ASSOCIATION'],
      },
    ],
  };

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
  async function getTableParams(isBatch: boolean) {
    const selectModules = await getModuleIds();
    const commonParams = {
      testPlanId: props.planId,
      projectId: appStore.currentProjectId,
      moduleIds: selectModules,
    };
    if (isBatch) {
      return {
        condition: {
          keyword: keyword.value,
          filter: propsRes.value.filter,
        },
        ...commonParams,
      };
    }
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
      ...commonParams,
    };
  }

  async function loadCaseList() {
    const tableParams = await getTableParams(false);
    setLoadListParams(tableParams);
    loadList();
    emit('getModuleCount', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }
  watch(
    () => props.activeModule,
    () => {
      loadCaseList();
    }
  );

  async function getModuleCount() {
    const tableParams = await getTableParams(false);
    emit('getModuleCount', {
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
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

  // 拖拽排序
  async function handleDragChange(params: DragSortParams) {
    try {
      // TODO 联调
      await sortFeatureCase({ ...params, testPlanId: props.planId });
      Message.success(t('caseManagement.featureCase.sortSuccess'));
      loadCaseList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 复制用例
  async function handleCopyCase(record: PlanDetailFeatureCaseItem) {
    try {
      // TODO 联调
      await associationCaseToPlan({
        functionalSelectIds: [record.caseId],
        testPlanId: props.planId,
      });
      Message.success(t('ms.case.associate.associateSuccess'));
      resetCaseList();
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
      // TODO 联调
      await disassociateCase({ testPlanId: props.planId, id: record.id });
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
          // TODO 联调
          await batchDisassociateCase({
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

  // 处理表格选中后批量操作
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = { ...params, selectIds: params?.selectedIds };
    switch (event.eventTag) {
      case 'execute':
        break;
      case 'disassociate':
        handleBatchDisassociateCase();
        break;
      case 'changeExecutor':
        break;
      case 'move':
        break;
      default:
        break;
    }
  }

  onBeforeMount(() => {
    loadCaseList();
  });

  defineExpose({
    resetSelector,
    loadCaseList,
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_DETAIL_API_CASE, columns.value, 'drawer', true);
</script>
