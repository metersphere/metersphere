<template>
  <div class="p-[16px]">
    <div class="mb-[16px] flex justify-end">
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('ms.case.associate.searchPlaceholder')"
        allow-clear
        class="mr-[8px] w-[240px]"
        @search="loadCaseList"
        @press-enter="loadCaseList"
        @clear="loadCaseList"
      />
      <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadCaseList">
        <template #icon>
          <icon-refresh class="text-[var(--color-text-4)]" />
        </template>
      </a-button>
    </div>
    <MsBaseTable
      v-bind="propsRes"
      :action-config="batchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
      @drag-change="handleDragChange"
      @filter-change="filterChange"
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
          v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])"
          v-model:model-value="record.lastExecResult"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full"
          @change="() => handleEditLastExecResult(record)"
        >
          <template #label>
            <span class="text-[var(--color-text-2)]"><ExecuteResult :execute-result="record.lastExecResult" /></span>
          </template>
          <a-option v-for="item in Object.values(executionResultMap)" :key="item.key" :value="item.key">
            <ExecuteResult :execute-result="item.key" />
          </a-option>
        </a-select>
        <span v-else class="text-[var(--color-text-2)]"><ExecuteResult :execute-result="record.lastExecResult" /></span>
      </template>
      <template #operation="{ record }">
        <MsButton
          v-permission="['PROJECT_TEST_PLAN:READ+EXECUTE']"
          type="text"
          class="!mr-0"
          @click="toCaseDetail(record)"
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
            t('testPlan.testPlanIndex.selectedCount', {
              count: batchParams.currentSelectCount || tableSelected.length,
            })
          }}
        </div>
      </template>
      <ExecuteForm v-model:form="batchExecuteForm" />
    </a-modal>
    <!-- 批量修改执行人 -->
    <a-modal
      v-model:visible="batchUpdateExecutorModalVisible"
      title-align="start"
      body-class="p-0"
      :cancel-button-props="{ disabled: batchLoading }"
      :ok-loading="batchLoading"
      :ok-button-props="{ disabled: batchUpdateExecutorDisabled }"
      :ok-text="t('common.update')"
      @before-ok="handleBatchUpdateExecutor"
      @close="resetBatchForm"
    >
      <template #title>
        {{ t('testPlan.featureCase.batchChangeExecutor') }}
        <div class="text-[var(--color-text-4)]">
          {{
            t('testPlan.testPlanIndex.selectedCount', {
              count: batchParams.currentSelectCount || tableSelected.length,
            })
          }}
        </div>
      </template>
      <a-form ref="batchUpdateExecutorFormRef" :model="batchUpdateExecutorForm" layout="vertical">
        <a-form-item
          field="userId"
          :label="t('testPlan.featureCase.executor')"
          :rules="[{ required: true, message: t('testPlan.featureCase.requestExecutorRequired') }]"
          asterisk-position="end"
          class="mb-0"
        >
          <MsSelect
            v-model:modelValue="batchUpdateExecutorForm.userId"
            mode="static"
            :placeholder="t('common.pleaseSelect')"
            :loading="executorLoading"
            :options="userOptions"
            :search-keys="['label']"
            allow-search
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import MsSelect from '@/components/business/ms-select';
  import ExecuteForm from '@/views/test-plan/testPlan/detail/featureCase/components/executeForm.vue';

  import {
    associationCaseToPlan,
    batchDisassociateCase,
    batchExecuteCase,
    batchUpdateCaseExecutor,
    disassociateCase,
    editLastExecResult,
    getPlanDetailFeatureCaseList,
    GetTestPlanUsers,
    sortFeatureCase,
  } from '@/api/modules/test-plan/testPlan';
  import { defaultExecuteForm } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ReviewUserItem } from '@/models/caseManagement/caseReview';
  import { DragSortParams, ModuleTreeNode } from '@/models/common';
  import type {
    ExecuteFeatureCaseFormParams,
    PlanDetailFeatureCaseItem,
    PlanDetailFeatureCaseListQueryParams,
  } from '@/models/testPlan/testPlan';
  import { LastExecuteResults } from '@/enums/caseEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';
  import {
    executionResultMap,
    getCaseLevels,
    getModules,
  } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    planId: string;
    moduleTree: ModuleTreeNode[];
    repeatCase: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'getModuleCount', params: PlanDetailFeatureCaseListQueryParams): void;
    (e: 'refresh'): void;
    (e: 'initModules'): void;
  }>();

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const tableStore = useTableStore();
  const { openModal } = useModal();

  const keyword = ref('');
  const tableParams = ref<PlanDetailFeatureCaseListQueryParams>({
    testPlanId: props.planId,
    projectId: appStore.currentProjectId,
  });

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE', 'PROJECT_TEST_PLAN:READ+ASSOCIATION'])
  );
  const columns: MsTableColumn = [
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
      width: 100,
      ellipsis: true,
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
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      ellipsis: true,
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
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 200 : 50,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, getTableQueryParams } = useTable(
    getPlanDetailFeatureCaseList,
    {
      scroll: { x: '100%' },
      tableKey: TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE,
      showSetting: true,
      selectable: hasOperationPermission.value,
      showSelectAll: true,
      draggable: hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) ? { type: 'handle', width: 32 } : undefined,
      heightUsed: 460,
      showSubdirectory: true,
    },
    (record) => {
      return {
        ...record,
        lastExecResult: record.lastExecResult ?? LastExecuteResults.PENDING,
        caseLevel: getCaseLevels(record.customFields),
        moduleId: getModules(record.moduleId, props.moduleTree),
      };
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
    ],
    moreAction: [
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
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }
  async function loadCaseList() {
    const selectModules = await getModuleIds();
    tableParams.value = {
      testPlanId: props.planId,
      projectId: appStore.currentProjectId,
      moduleIds: selectModules,
      keyword: keyword.value,
    };
    setLoadListParams(tableParams.value);
    loadList();
    emit('getModuleCount', {
      ...tableParams.value,
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

  function filterChange() {
    tableParams.value = { ...tableParams.value, filter: propsRes.value.filter };
    emit('getModuleCount', {
      ...tableParams.value,
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

  function resetCaseList() {
    resetSelector();
    emit('getModuleCount', {
      ...tableParams.value,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
    loadList();
  }

  // 拖拽排序
  async function handleDragChange(params: DragSortParams) {
    try {
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

  // 更新执行结果
  async function handleEditLastExecResult(record: PlanDetailFeatureCaseItem) {
    try {
      await editLastExecResult({
        id: record.id,
        testPlanId: props.planId,
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
      title: t('caseManagement.caseReview.disassociateConfirmTitle', { count: batchParams.value.currentSelectCount }),
      content: t('testPlan.featureCase.batchDisassociateTipContent'),
      okText: t('common.cancelLink'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          await batchDisassociateCase({
            ...batchParams.value,
            ...tableParams.value,
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

  // 批量执行
  const batchLoading = ref(false);
  const batchExecuteModalVisible = ref(false);
  const batchExecuteForm = ref<ExecuteFeatureCaseFormParams>({ ...defaultExecuteForm });
  async function handleBatchExecute() {
    try {
      batchLoading.value = true;
      await batchExecuteCase({
        ...batchParams.value,
        ...tableParams.value,
        ...batchExecuteForm.value,
        notifier: batchExecuteForm.value?.commentIds?.join(';'),
        selectIds: batchParams.value.selectedIds,
      });
      Message.success(t('common.updateSuccess'));
      resetSelector();
      loadList();
      emit('refresh');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchLoading.value = false;
    }
  }

  // 批量修改执行人
  const batchUpdateExecutorFormRef = ref<FormInstance>();
  const batchUpdateExecutorModalVisible = ref(false);
  const batchUpdateExecutorForm = ref<{ userId: string }>({ userId: '' });
  const batchUpdateExecutorDisabled = computed(() => !batchUpdateExecutorForm.value.userId.length);
  const userOptions = ref<SelectOptionData[]>([]);
  const executorLoading = ref(false);
  async function initUserOptions() {
    try {
      executorLoading.value = true;
      const res = await GetTestPlanUsers(appStore.currentProjectId, '');
      userOptions.value = res.map((e: ReviewUserItem) => ({ label: e.name, value: e.id }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      executorLoading.value = false;
    }
  }

  async function handleBatchUpdateExecutor() {
    batchUpdateExecutorFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchLoading.value = true;
          await batchUpdateCaseExecutor({
            ...batchParams.value,
            ...tableParams.value,
            ...batchUpdateExecutorForm.value,
          });
          Message.success(t('common.updateSuccess'));
          resetSelector();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          batchLoading.value = false;
        }
      }
    });
  }

  function resetBatchForm() {
    batchExecuteForm.value = { ...defaultExecuteForm };
    batchUpdateExecutorForm.value = { userId: '' };
  }

  // 处理表格选中后批量操作
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = { ...params, selectIds: params?.selectedIds };
    switch (event.eventTag) {
      case 'execute':
        batchExecuteModalVisible.value = true;
        break;
      case 'disassociate':
        handleBatchDisassociateCase();
        break;
      case 'changeExecutor':
        batchUpdateExecutorModalVisible.value = true;
        break;
      default:
        break;
    }
  }

  // 去用例详情页面
  function toCaseDetail(record: PlanDetailFeatureCaseItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: record.caseId,
        testPlanCaseId: record.id,
      },
      state: {
        params: JSON.stringify(getTableQueryParams()),
      },
    });
  }

  onBeforeMount(() => {
    loadCaseList();
    initUserOptions();
  });

  defineExpose({
    resetSelector,
    loadCaseList,
  });

  await tableStore.initColumn(TableKeyEnum.TEST_PLAN_DETAIL_FEATURE_CASE_TABLE, columns, 'drawer', true);
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
</style>
