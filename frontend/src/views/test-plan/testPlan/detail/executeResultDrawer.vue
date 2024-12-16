<template>
  <MsDrawer v-model:visible="visible" :width="1200" :footer="false">
    <template #title>
      <div class="flex flex-1 items-center gap-[8px] overflow-hidden">
        <a-tag :color="executeResultMap[detail.result]?.color">
          <div class="text-[var(--color-text-1)]">
            {{ t(executeResultMap[detail.result]?.label || '-') }}
          </div>
        </a-tag>
        <div class="one-line-text flex-1">{{ detail.taskName }}</div>
      </div>
      <div class="flex justify-end">
        <MsButton type="icon" status="secondary" class="!rounded-[var(--border-radius-small)]" @click="init">
          <MsIcon type="icon-icon_reset_outlined" class="mr-[8px]" size="14" />
          {{ t('common.refresh') }}
        </MsButton>
      </div>
    </template>
    <a-spin :loading="loading" class="block min-h-[200px]">
      <MsDescription :descriptions="detail.description" :column="2" :line-gap="8" one-line-value>
        <template #value="{ item }">
          <execStatus v-if="item.key === 'status'" :status="detail.status" size="small" />
          <a-select
            v-else-if="item.key === 'testPlan'"
            v-model:model-value="activePlan"
            :options="testPlanGroups"
            size="small"
            @change="handlePlanChange"
          ></a-select>
          <executeRatePopper
            v-else-if="item.key === 'rate'"
            v-model:visible="executeRateVisible"
            v-model:record="detail"
            :execute-case-count="detail.executeCaseCount"
            class="inline-block"
          />
          <a-tooltip
            v-else
            :content="`${item.value}`"
            :disabled="item.value === undefined || item.value === null || item.value?.toString() === ''"
            :position="item.tooltipPosition ?? 'tl'"
          >
            <div class="w-[fit-content]">
              {{ item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value }}
            </div>
          </a-tooltip>
        </template>
      </MsDescription>
      <div class="flex items-center justify-between">
        <MsTab
          v-model:active-key="activeTable"
          :content-tab-list="showContentTabList"
          :show-badge="false"
          class="testPlan-execute-tab no-content"
          @change="searchList"
        />
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('report.detail.caseDetailSearchPlaceholder')"
          allow-clear
          class="w-[240px]"
          @search="searchList"
          @press-enter="searchList"
          @clear="searchList"
        />
      </div>
      <div class="mt-[8px]">
        <MsBaseTable
          v-bind="currentCaseTable.propsRes.value"
          :row-class="getRowClass"
          v-on="currentCaseTable.propsEvent.value"
        >
          <template #num="{ record }">
            <MsButton type="text" @click="toDetail(record)">{{ record.num }}</MsButton>
          </template>
          <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
            <caseLevel :case-level="filterContent.value" />
          </template>
          <template #priority="{ record }">
            <caseLevel :case-level="record.priority" />
          </template>
          <template #[FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS]="{ filterContent }">
            <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
          </template>

          <template #lastExecResult="{ record }">
            <ExecutionStatus
              :module-type="ReportEnum.API_REPORT"
              :status="record.executeResult"
              :class="[!record.executeResult ? '' : 'cursor-pointer']"
              @click="showReport(record)"
            />
          </template>
        </MsBaseTable>
      </div>
    </a-spin>
  </MsDrawer>
  <CaseAndScenarioReportDrawer
    v-if="reportVisible"
    v-model:visible="reportVisible"
    :report-id="apiReportId"
    do-not-show-share
    :is-scenario="activeTable === 'scenario'"
    :report-detail="activeTable === 'scenario' ? reportScenarioDetail : reportCaseDetail"
    :get-report-step-detail="activeTable === 'scenario' ? reportStepDetail : reportCaseStepDetail"
  />
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import execStatus from '@/views/taskCenter/component/execStatus.vue';
  import executeRatePopper from '@/views/taskCenter/component/executeRatePopper.vue';

  import {
    getApiPage,
    getScenarioPage,
    reportCaseDetail,
    reportCaseStepDetail,
    reportScenarioDetail,
    reportStepDetail,
  } from '@/api/modules/test-plan/report';
  import { getTaskResult } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { ApiOrScenarioCaseItem } from '@/models/testPlan/report';
  import { PlanDetailExecuteHistoryItem } from '@/models/testPlan/testPlan';
  import { ReportEnum } from '@/enums/reportEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { casePriorityOptions, lastReportStatusListOptions } from '@/views/api-test/components/config';
  import { executeResultMap, executeStatusMap } from '@/views/taskCenter/component/config';

  const CaseAndScenarioReportDrawer = defineAsyncComponent(
    () => import('@/views/api-test/components/caseAndScenarioReportDrawer.vue')
  );

  const props = defineProps<{
    planDetail: PlanDetailExecuteHistoryItem;
  }>();

  const { openNewPage } = useOpenNewPage();
  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const loading = ref(false);
  const detail = ref<any>({ description: [] });
  const testPlanGroups = ref<SelectOptionData[]>([]);
  const executeRateVisible = ref(false);
  const activePlan = ref('');
  const activePlanCaseTotal = ref(0);
  const activePlanScenarioTotal = ref(0);

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      width: 100,
    },
    {
      title: 'common.name',
      dataIndex: 'name',
      width: 150,
      showTooltip: true,
    },
    {
      title: 'report.detail.level',
      dataIndex: 'priority',
      slotName: 'priority',
      filterConfig: {
        options: casePriorityOptions,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      width: 150,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUser',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      filterConfig: {
        options: lastReportStatusListOptions.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_LAST_EXECUTE_STATUS,
        emptyFilter: true,
      },
      width: 150,
    },
  ];

  const activeTable = ref<'case' | 'scenario'>('case');
  const contentTabList = [
    { value: 'case', label: t('report.detail.apiCaseDetails') },
    { value: 'scenario', label: t('report.detail.scenarioCaseDetails') },
  ];
  const showContentTabList = computed(() =>
    contentTabList.filter((item) => {
      if (item.value === 'case') {
        return activePlanCaseTotal.value > 0;
      }
      return activePlanScenarioTotal.value > 0;
    })
  );
  const keyword = ref('');

  const useApiTable = useTable(getApiPage, {
    scroll: { x: '100%' },
    columns,
    showSelectorAll: false,
    heightUsed: 236,
    showSetting: false,
    paginationSize: 'mini',
  });

  const useScenarioTable = useTable(getScenarioPage, {
    scroll: { x: '100%' },
    columns,
    showSelectorAll: false,
    showSetting: false,
    heightUsed: 236,
    paginationSize: 'mini',
  });
  const currentCaseTable = computed(() => (activeTable.value === 'case' ? useApiTable : useScenarioTable));

  // 显示执行报告
  const reportVisible = ref(false);

  const apiReportId = ref<string>('');
  const selectedReportId = ref<string>('');

  function showReport(record: ApiOrScenarioCaseItem) {
    if (!record.reportId) {
      return;
    }
    if (!record.executeResult || record.executeResult === 'STOPPED') return;
    reportVisible.value = true;
    apiReportId.value = record.reportId;
    selectedReportId.value = record.reportId;
  }

  function getRowClass(record: ApiOrScenarioCaseItem) {
    return record.reportId === selectedReportId.value ? 'selected-row-class' : '';
  }

  function searchList() {
    currentCaseTable.value.setLoadListParams({
      keyword: keyword.value,
      reportId: activePlan.value || detail.value.reportId,
    });
    currentCaseTable.value.loadList();
  }

  function handlePlanChange(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    const plan = testPlanGroups.value.find((item) => item.value === value);
    if (plan) {
      activePlanCaseTotal.value = plan.apiCaseTotal;
      activePlanScenarioTotal.value = plan.apiScenarioTotal;
      activeTable.value = activePlanCaseTotal.value > 0 ? 'case' : 'scenario';
      nextTick(() => {
        searchList();
      });
    }
  }

  // 去用例详情页面
  function toDetail(record: ApiOrScenarioCaseItem) {
    if (activeTable.value === 'scenario') {
      openNewPage(ApiTestRouteEnum.API_TEST_SCENARIO, {
        id: record.id,
        pId: record.projectId,
      });
    } else {
      openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
        cId: record.id,
        pId: record.projectId,
      });
    }
  }

  async function init() {
    try {
      loading.value = true;
      const res = await getTaskResult(props.planDetail.id);
      detail.value = {
        description: [
          {
            label: t('ms.taskCenter.executeStatus'),
            key: 'status',
            value: t(executeStatusMap[res.status].label),
          },
          {
            label: t('ms.taskCenter.taskCreateTime'),
            value: res.createTime ? dayjs(res.createTime).format('YYYY-MM-DD HH:mm:ss') : '-',
          },
          {
            label: t('ms.taskCenter.operationUser'),
            value: res.createUser,
          },
          {
            label: t('ms.taskCenter.taskStartTime'),
            value: res.startTime ? dayjs(res.startTime).format('YYYY-MM-DD HH:mm:ss') : '-',
          },
          {
            label: t('ms.taskCenter.executeFinishedRate'),
            key: 'rate',
            value: res.result,
          },
          {
            label: t('ms.taskCenter.taskEndTime'),
            value: res.endTime ? dayjs(res.endTime).format('YYYY-MM-DD HH:mm:ss') : '-',
          },
        ] as Description[],
        ...res,
      };
      if (res.childPlans.length) {
        detail.value.description.push({
          label: t('testPlan.testPlanIndex.testPlan'),
          key: 'testPlan',
          value: '',
        });
        testPlanGroups.value = res.childPlans.map((item) => ({
          value: item.id,
          label: item.name,
          apiCaseTotal: item.apiCaseTotal,
          apiScenarioTotal: item.apiScenarioTotal,
        }));
        activePlan.value = res.childPlans[0]?.id;
        activePlanCaseTotal.value = res.childPlans[0]?.apiCaseTotal;
        activePlanScenarioTotal.value = res.childPlans[0]?.apiScenarioTotal;
      } else {
        testPlanGroups.value = [];
        activePlanCaseTotal.value = res.apiCaseTotal;
        activePlanScenarioTotal.value = res.apiScenarioTotal;
      }
      activeTable.value = activePlanCaseTotal.value > 0 ? 'case' : 'scenario';
      searchList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => visible.value,
    (val) => {
      if (props.planDetail.id && val) {
        init();
      }
    },
    { immediate: true }
  );
</script>

<style lang="less">
  .testPlan-execute-tab {
    .arco-tabs-tab:first-child {
      margin-left: 0;
    }
  }
</style>

<style lang="less" scoped>
  :deep(.ms-description-item) {
    @apply items-center;

    margin-bottom: 8px;
    font-size: 12px;
    line-height: 16px;
  }
</style>
