<template>
  <MsBaseTable v-bind="propsRes" v-on="propsEvent">
    <template #caseLevel="{ record }">
      <CaseLevel :case-level="record.priority" />
    </template>
    <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
      <ExecuteResult :execute-result="filterContent.key" />
    </template>
    <template #lastExecResult="{ record }">
      <ExecuteResult :execute-result="record.executeResult" />
      <MsButton class="ml-[8px]" :disabled="!props.isPreview" @click="openExecuteHistory(record)">{{
        t('common.detail')
      }}</MsButton>
    </template>
  </MsBaseTable>
  <MsDrawer
    v-model:visible="showDetailVisible"
    :title="t('ms.case.associate.title')"
    :width="1200"
    :footer="false"
    no-content-padding
    unmount-on-close
  >
    <!-- TODO 等待联调 后台没出接口 -->
    <ExecutionHistory
      :extra-params="{
        caseId: '',
        id: '',
        testPlanId: '',
      }"
      :load-list-fun="executeHistory"
    />
  </MsDrawer>
</template>

<script setup lang="ts">
  import { onBeforeMount } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import ExecutionHistory from '@/views/test-plan/testPlan/detail/featureCase/detail/executionHistory/index.vue';

  import { getReportFeatureCaseList, getReportShareFeatureCaseList } from '@/api/modules/test-plan/report';
  import { executeHistory } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';

  import { FeatureCaseItem } from '@/models/testPlan/report';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';
  import { detailTableExample } from '@/views/test-plan/report/detail/component/reportConfig';

  const props = defineProps<{
    reportId: string;
    shareId?: string;
    isPreview?: boolean;
  }>();
  const { t } = useI18n();
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
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      filterConfig: {
        valueKey: 'key',
        labelKey: 'statusText',
        options: Object.values(executionResultMap),
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      width: 150,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      ellipsis: true,
      showTooltip: true,
      width: 200,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      width: 120,
    },

    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUser',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      width: 100,
    },
  ];
  const reportFeatureCaseList = () => {
    return !props.shareId ? getReportFeatureCaseList : getReportShareFeatureCaseList;
  };
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(reportFeatureCaseList(), {
    scroll: { x: '100%' },
    columns,
    heightUsed: 20,
    showSelectorAll: false,
  });

  async function loadCaseList() {
    setLoadListParams({ reportId: props.reportId, shareId: props.shareId ?? undefined });
    loadList();
  }

  watchEffect(() => {
    if (props.reportId && props.isPreview) {
      loadCaseList();
    } else {
      propsRes.value.data = detailTableExample[ReportCardTypeEnum.FUNCTIONAL_DETAIL];
    }
  });

  const showDetailVisible = ref<boolean>(false);

  const detailRecord = ref();

  function openExecuteHistory(record: FeatureCaseItem) {
    detailRecord.value = record;
    showDetailVisible.value = true;
  }
</script>
