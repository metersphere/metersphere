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
    <MsBaseTable v-bind="propsRes" :action-config="batchActions" v-on="propsEvent" @batch-action="handleTableBatch">
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
        <!-- TODO: 修改permission -->
        <a-select
          v-if="hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+UPDATE'])"
          v-model:model-value="record.lastExecResult"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full"
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
      <template #operation>
        <MsButton v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']" type="text" class="!mr-0">
          {{ t('common.execute') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']" direction="vertical" :margin="8"></a-divider>
        <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']" type="text" class="!mr-0">
          {{ t('common.cancelLink') }}
        </MsButton>
        <!-- TODO: 修改permission -->
        <a-divider
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
          direction="vertical"
          :margin="8"
        ></a-divider>
        <MsButton v-permission="['PROJECT_API_DEFINITION_CASE:READ+ADD']" type="text" class="!mr-0">
          {{ t('common.copy') }}
        </MsButton>
      </template>
    </MsBaseTable>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';

  import { getPlanDetailFeatureCaseList } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ModuleTreeNode } from '@/models/common';
  import type { PlanDetailFeatureCaseItem, PlanDetailFeatureCaseListQueryParams } from '@/models/testPlan/testPlan';
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
  }>();

  const emit = defineEmits<{
    (e: 'init', params: PlanDetailFeatureCaseListQueryParams): void;
  }>();

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const tableStore = useTableStore();

  const keyword = ref('');

  // TODO: 复制的Permission
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
        caseLevel: getCaseLevels(record.customFields),
        moduleId: getModules(record.moduleId, props.moduleTree),
      };
    }
  );
  // TODO: permission
  const batchActions = {
    baseAction: [
      {
        label: 'common.execute',
        eventTag: 'execute',
        permission: ['PROJECT_TEST_PLAN:READ+EXECUTE'],
      },
      {
        label: 'testPlan.featureCase.sort',
        eventTag: 'sort',
      },
      {
        label: 'testPlan.featureCase.changeExecutor',
        eventTag: 'changeExecutor',
      },
    ],
    moreAction: [
      {
        label: 'common.cancelLink',
        eventTag: 'disassociate',
        permission: ['PROJECT_TEST_PLAN:READ+ASSOCIATION'],
      },
      {
        isDivider: true,
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
      },
    ],
  };

  const tableSelected = ref<(string | number)[]>([]); // 表格选中的
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });
  // 处理表格选中后批量操作
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'execute':
        break;
      default:
        break;
    }
  }

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
    const params: PlanDetailFeatureCaseListQueryParams = {
      testPlanId: props.planId,
      projectId: appStore.currentProjectId,
      moduleIds: selectModules,
      keyword: keyword.value,
    };
    setLoadListParams(params);
    loadList();
    emit('init', {
      ...params,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }
  onBeforeMount(() => {
    loadCaseList();
  });

  // 去用例详情页面
  function toCaseDetail(record: PlanDetailFeatureCaseItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: record.id,
      },
      state: {
        params: JSON.stringify(getTableQueryParams()),
      },
    });
  }

  defineExpose({
    resetSelector,
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
