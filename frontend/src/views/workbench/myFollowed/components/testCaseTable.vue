<template>
  <MsCard auto-height simple>
    <div class="flex items-center justify-between">
      <div class="cursor-pointer font-medium text-[var(--color-text-1)]" @click="goTestCase">
        {{ t('ms.workbench.myFollowed.feature.TEST_CASE') }}
      </div>
    </div>
    <ms-base-table v-bind="propsRes" ref="tableRef" filter-icon-align-left class="mt-[16px]" v-on="propsEvent">
      <template #num="{ record }">
        <span type="text" class="one-line-text cursor-pointer px-0 text-[rgb(var(--primary-5))]">
          {{ record.num }}
        </span>
      </template>
      <template #name="{ record }">
        <div class="one-line-text">{{ record.name }}</div>
      </template>
      <template #caseLevel="{ record }">
        <span class="text-[var(--color-text-2)]">
          <caseLevel :case-level="record.caseLevel" />
        </span>
      </template>
      <!-- 用例等级 -->
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
        <caseLevel :case-level="filterContent.text" />
      </template>
      <!-- 执行结果 -->
      <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
        <ExecuteStatusTag :execute-result="filterContent.value" />
      </template>
      <!-- 评审结果 -->
      <template #reviewStatus="{ record }">
        <MsIcon
          :type="statusIconMap[record.reviewStatus]?.icon || ''"
          class="mr-1"
          :class="[statusIconMap[record.reviewStatus].color]"
        ></MsIcon>
        <span>{{ statusIconMap[record.reviewStatus]?.statusText || '' }} </span>
      </template>
      <template #lastExecuteResult="{ record }">
        <ExecuteStatusTag v-if="record.lastExecuteResult" :execute-result="record.lastExecuteResult" />
        <span v-else>-</span>
      </template>
    </ms-base-table>
  </MsCard>
</template>

<script setup lang="ts">
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import { MsTableProps } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteStatusTag from '@/components/business/ms-case-associate/executeResult.vue';

  import { getCaseList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { CaseManagementTable } from '@/models/caseManagement/featureCase';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import {
    executionResultMap,
    getCaseLevels,
    statusIconMap,
  } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    project: string;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const executeResultOptions = computed(() => {
    return Object.keys(executionResultMap).map((key) => {
      return {
        value: key,
        label: executionResultMap[key].statusText,
      };
    });
  });
  const reviewResultOptions = computed(() => {
    return Object.keys(statusIconMap).map((key) => {
      return {
        value: key,
        label: statusIconMap[key].statusText,
      };
    });
  });

  const columns: MsTableColumn = [
    {
      'title': 'ID',
      'dataIndex': 'num',
      'slotName': 'num',
      'sortIndex': 1,
      'fixed': 'left',
      'width': 150,
      'showTooltip': true,
      'columnSelectorDisabled': true,
      'filter-icon-align-left': true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 180,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnLevel',
      slotName: 'caseLevel',
      dataIndex: 'caseLevel',
      filterConfig: {
        options: [],
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnReviewResult',
      dataIndex: 'reviewStatus',
      slotName: 'reviewStatus',
      filterConfig: {
        options: reviewResultOptions.value,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_RESULT,
      },
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      dataIndex: 'lastExecuteResult',
      slotName: 'lastExecuteResult',
      filterConfig: {
        options: executeResultOptions.value,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      showTooltip: true,
      width: 200,
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
  ];

  const tableProps = ref<Partial<MsTableProps<CaseManagementTable>>>({
    columns,
    selectable: false,
    showSetting: false,
    paginationSize: 'mini',
  });

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getCaseList, tableProps.value, (record) => {
    return {
      ...record,
      tags: (record.tags || []).map((item: string, i: number) => {
        return {
          id: `${record.id}-${i}`,
          name: item,
        };
      }),
      visible: false,
      showModuleTree: false,
      caseLevel: getCaseLevels(record.customFields),
    };
  });

  function init() {
    setLoadListParams({
      projectId: props.project,
      viewId: 'my_follow',
    });
    loadList();
  }

  onBeforeMount(() => {
    init();
  });

  function goTestCase() {
    openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT_CASE, {
      showType: 'list',
      view: 'my_follow',
    });
  }
</script>

<style lang="less" scoped></style>
