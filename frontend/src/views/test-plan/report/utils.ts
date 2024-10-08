import { TableSortable } from '@arco-design/web-vue';
import { cloneDeep } from 'lodash-es';

import type { MsTableColumn } from '@/components/pure/ms-table/type';

import type { countDetail } from '@/models/testPlan/testPlanReport';
import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';
import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';

export function getSummaryDetail(detailCount: countDetail) {
  if (detailCount) {
    const { success, error, fakeError, pending, block } = detailCount;
    // 已执行用例
    const hasExecutedCase = success + error + fakeError + block;
    // 用例总数
    const caseTotal = hasExecutedCase + pending;
    // 执行率
    const executedCount = (hasExecutedCase / caseTotal) * 100;
    const apiExecutedRate = `${Number.isNaN(executedCount) ? 0 : executedCount.toFixed(2)}%`;
    // 通过率
    const successCount = (success / caseTotal) * 100;
    const successRate = `${Number.isNaN(successCount) ? 0 : successCount.toFixed(2)}%`;
    return {
      hasExecutedCase,
      caseTotal,
      apiExecutedRate,
      successRate,
      pending,
      success,
    };
  }
  return {
    hasExecutedCase: 0,
    caseTotal: 0,
    apiExecutedRate: 0,
    successRate: 0,
    pending: 0,
    success: 0,
  };
}

// 获取用例明细的表头
export function getFeatureColumns(isGroup: boolean, isPreview: boolean): MsTableColumn {
  const sortableConfig = computed<TableSortable | undefined>(() => {
    return isPreview
      ? {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        }
      : undefined;
  });
  const staticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      width: 150,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: cloneDeep(sortableConfig.value),
      width: 180,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      filterConfig: {
        valueKey: 'key',
        labelKey: 'statusText',
        options: isPreview ? Object.values(executionResultMap) : [],
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      showInTable: true,
      showDrag: true,
      width: 150,
    },
  ];

  const lastStaticColumns: MsTableColumn = [
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      ellipsis: true,
      showTooltip: true,
      showInTable: true,
      showDrag: true,
      width: 200,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      showInTable: true,
      showDrag: true,
      width: 120,
    },

    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUser',
      showTooltip: true,
      showInTable: true,
      showDrag: true,
      width: 150,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      showInTable: true,
      showDrag: true,
      width: 100,
    },
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 30,
    },
  ];

  const testPlanNameColumns: MsTableColumn = [
    {
      title: 'report.plan.name',
      dataIndex: 'planName',
      showTooltip: true,
      showInTable: true,
      showDrag: false,
      columnSelectorDisabled: true,
      width: 200,
    },
  ];

  if (isGroup) {
    return [...staticColumns, ...testPlanNameColumns, ...lastStaticColumns];
  }
  return [...staticColumns, ...lastStaticColumns];
}

// 获取接口明细的表头
export function getApiDetailColumn(isGroup: boolean, isPreview: boolean): MsTableColumn {
  const staticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      width: 100,
      showInTable: true,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'common.name',
      dataIndex: 'name',
      width: 150,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'report.detail.level',
      dataIndex: 'priority',
      slotName: 'priority',
      filterConfig: {
        options: isPreview ? casePriorityOptions : [],
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      filterConfig: {
        options: isPreview ? lastReportStatusListOptions.value : [],
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
      },
      width: 150,
      showInTable: true,
      showDrag: true,
    },
  ];
  const testPlanNameColumns: MsTableColumn = [
    {
      title: 'report.plan.name',
      dataIndex: 'planName',
      showTooltip: true,
      width: 200,
      showInTable: true,
      showDrag: false,
      columnSelectorDisabled: true,
    },
  ];
  const lastStaticColumns: MsTableColumn = [
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      showTooltip: true,
      width: 200,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUser',
      showTooltip: true,
      width: 130,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      slotName: 'bugCount',
      width: 100,
      showInTable: true,
      showDrag: true,
    },
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 30,
    },
  ];

  if (isGroup) {
    return [...staticColumns, ...testPlanNameColumns, ...lastStaticColumns];
  }
  return [...staticColumns, ...lastStaticColumns];
}

export default {};
