<template>
  <a-spin :loading="loading" class="block">
    <div id="report-detail" class="p-[16px]">
      <div class="report-header">
        <div class="flex-1 break-all">{{ detail.name }}</div>
        <div class="one-line-text">
          <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</span>
          {{ detail.startTime ? dayjs(detail.startTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTimeTo') }}</span>
          {{ detail.endTime ? dayjs(detail.endTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </div>
      </div>
      <div class="analysis-wrapper" :data-cards="cardCount">
        <div class="analysis min-w-[330px]">
          <div class="block-title">{{ t('report.detail.api.reportAnalysis') }}</div>
          <ReportMetricsItem
            v-for="analysisItem in reportAnalysisList"
            :key="analysisItem.name"
            :item-info="analysisItem"
          />
        </div>
        <div class="analysis min-w-[410px]">
          <ExecuteAnalysis :detail="detail" :animation="false" />
        </div>
        <div v-if="functionalCaseTotal" class="analysis min-w-[330px]">
          <div class="block-title">{{ t('report.detail.useCaseAnalysis') }}</div>
          <div class="flex">
            <div class="mr-[24px] flex-1">
              <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="pending" />
              <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="success" />
              <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="block" />
              <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="error" class="!mb-0" />
            </div>
            <div class="relative">
              <div class="charts">
                <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                <div class="flex justify-center text-[16px] font-medium">
                  <div class="one-line-text max-w-[100px] text-[var(--color-text-1)]">{{ functionCasePassRate }} </div>
                </div>
              </div>
              <div class="flex h-full w-full items-center justify-center">
                <MsChart width="100px" height="100px" :options="functionCaseOptions" />
              </div>
            </div>
          </div>
        </div>
        <div v-if="apiCaseTotal" class="analysis min-w-[330px]">
          <div class="block-title">{{ t('report.detail.apiUseCaseAnalysis') }}</div>
          <div class="flex">
            <div class="mr-[24px] flex-1">
              <SingleStatusProgress type="API" :detail="detail" status="pending" />
              <SingleStatusProgress type="API" :detail="detail" status="success" />
              <SingleStatusProgress type="API" :detail="detail" status="fakeError" />
              <SingleStatusProgress type="API" :detail="detail" status="error" class="!mb-0" />
            </div>
            <div class="relative">
              <div class="charts">
                <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                <div class="flex justify-center text-[16px] font-medium">
                  <div class="one-line-text max-w-[100px] text-[var(--color-text-1)]">{{ apiCasePassRate }} </div>
                </div>
              </div>
              <div class="flex h-full w-full items-center justify-center">
                <MsChart width="100px" height="100px" :options="apiCaseOptions" />
              </div>
            </div>
          </div>
        </div>
        <div v-if="scenarioCaseTotal" class="analysis min-w-[330px]">
          <div class="block-title">{{ t('report.detail.scenarioUseCaseAnalysis') }}</div>
          <div class="flex">
            <div class="mr-[24px] flex-1">
              <SingleStatusProgress type="SCENARIO" :detail="detail" status="pending" />
              <SingleStatusProgress type="SCENARIO" :detail="detail" status="success" />
              <SingleStatusProgress type="SCENARIO" :detail="detail" status="fakeError" />
              <SingleStatusProgress type="SCENARIO" :detail="detail" status="error" class="!mb-0" />
            </div>
            <div class="relative">
              <div class="charts">
                <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                <div class="flex justify-center text-[16px] font-medium">
                  <div class="one-line-text max-w-[100px] text-[var(--color-text-1)]">{{ scenarioCasePassRate }} </div>
                </div>
              </div>
              <div class="flex h-full w-full items-center justify-center">
                <MsChart width="100px" height="100px" :options="scenarioCaseOptions" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-[16px]">
        <div v-for="item of innerCardList" v-show="showItem(item)" :key="item.id" class="card-item mt-[16px]">
          <div class="wrapper-preview-card">
            <div class="flex items-center justify-between">
              <div v-if="item.value !== ReportCardTypeEnum.CUSTOM_CARD" class="mb-[8px] font-medium">
                {{ t(item.label) }}
              </div>
            </div>
            <ReportDetailTable
              v-if="item.value === ReportCardTypeEnum.SUB_PLAN_DETAIL"
              v-model:current-mode="currentMode"
              :report-id="detail.id"
              :share-id="shareId"
              is-preview
            />
            <div v-else-if="item.value === ReportCardTypeEnum.SUMMARY" v-html="getContent(item).content"></div>
            <MsBaseTable v-else-if="item.value === ReportCardTypeEnum.BUG_DETAIL" v-bind="bugTableProps"> </MsBaseTable>
            <MsBaseTable v-else-if="item.value === ReportCardTypeEnum.FUNCTIONAL_DETAIL" v-bind="caseTableProps">
              <template #caseLevel="{ record }">
                <CaseLevel :case-level="record.priority" />
              </template>
              <template #lastExecResult="{ record }">
                <ExecuteResult :execute-result="record.executeResult" />
              </template>
            </MsBaseTable>
            <MsBaseTable
              v-else-if="item.value === ReportCardTypeEnum.API_CASE_DETAIL"
              v-bind="useApiTable.propsRes.value"
            >
              <template #priority="{ record }">
                <caseLevel :case-level="record.priority" />
              </template>

              <template #lastExecResult="{ record }">
                <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="record.executeResult" />
              </template>
            </MsBaseTable>
            <MsBaseTable
              v-else-if="item.value === ReportCardTypeEnum.SCENARIO_CASE_DETAIL"
              v-bind="useScenarioTable.propsRes.value"
            >
              <template #priority="{ record }">
                <caseLevel :case-level="record.priority" />
              </template>

              <template #lastExecResult="{ record }">
                <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="record.executeResult" />
              </template>
            </MsBaseTable>
            <div v-else-if="item.value === ReportCardTypeEnum.CUSTOM_CARD" v-html="item.content"></div>
          </div>
        </div>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteResult from '@/components/business/ms-case-associate/executeResult.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import SingleStatusProgress from '@/views/test-plan/report/component/singleStatusProgress.vue';
  import ExecuteAnalysis from '@/views/test-plan/report/detail/component/system-card/executeAnalysis.vue';
  import ReportDetailTable from '@/views/test-plan/report/detail/component/system-card/reportDetailTable.vue';
  import ReportMetricsItem from '@/views/test-plan/report/detail/component/system-card/ReportMetricsItem.vue';

  import {
    getApiPage,
    getReportBugList,
    getReportDetail,
    getReportFeatureCaseList,
    getReportLayout,
    getReportShareBugList,
    getReportShareFeatureCaseList,
    getScenarioPage,
  } from '@/api/modules/test-plan/report';
  import {
    commonConfig,
    defaultCount,
    defaultReportDetail,
    seriesConfig,
    statusConfig,
    toolTipConfig,
  } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import type {
    configItem,
    countDetail,
    PlanReportDetail,
    ReportMetricsItemModel,
    StatusListType,
  } from '@/models/testPlan/testPlanReport';
  import { customValueForm } from '@/models/testPlan/testPlanReport';
  import { ReportEnum } from '@/enums/reportEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { defaultGroupConfig, defaultSingleConfig } from './component/reportConfig';
  import { getSummaryDetail } from '@/views/test-plan/report/utils';
  import exportPdf from '@/workers/exportPDF/exportPDFWorker';

  const { t } = useI18n();
  const route = useRoute();

  const innerCardList = defineModel<configItem[]>('cardList', {
    default: [],
  });

  const detail = ref<PlanReportDetail>({ ...cloneDeep(defaultReportDetail) });
  const reportId = ref<string>(route.query.id as string);
  const isGroup = computed(() => route.query.type === 'GROUP');
  const loading = ref<boolean>(false);
  const richText = ref<{ summary: string; richTextTmpFileIds?: string[] }>({
    summary: '',
  });

  const reportForm = ref({
    reportName: '',
  });

  /**
   * 分享share
   */
  const shareId = ref<string>(route.query.shareId as string);

  // 功能用例分析
  const functionCaseOptions = ref({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
    animation: false, // 关闭渲染动画
    series: {
      ...seriesConfig,
      data: [
        {
          value: 0,
          name: t('common.success'),
          itemStyle: {
            color: '#00C261',
          },
        },
      ],
    },
  });
  // 接口用例分析
  const apiCaseOptions = ref({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
    animation: false, // 关闭渲染动画
    series: {
      ...seriesConfig,
      data: [
        {
          value: 0,
          name: t('common.success'),
          itemStyle: {
            color: '#00C261',
          },
        },
      ],
    },
  });
  // 场景用例分析
  const scenarioCaseOptions = ref({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
    animation: false, // 关闭渲染动画
    series: {
      ...seriesConfig,
      data: [
        {
          value: 0,
          name: t('common.success'),
          itemStyle: {
            color: '#00C261',
          },
        },
      ],
    },
  });

  // 获取通过率
  function getPassRateData(caseDetailCount: countDetail) {
    const caseCountDetail = caseDetailCount || defaultCount;
    const passRateData = statusConfig.filter((item) => ['success'].includes(item.value));
    const { success } = caseCountDetail;
    const valueList = success ? statusConfig : passRateData;
    const chartBorderWidth = valueList.filter((e) => Number(caseCountDetail[e.value]) > 0).length === 1 ? 0 : 2;
    return valueList
      .filter((item) => caseCountDetail[item.value] > 0)
      .map((item: StatusListType) => {
        return {
          value: caseCountDetail[item.value] || 0,
          name: t(item.label),
          itemStyle: {
            color: success ? item.color : '#D4D4D8',
            borderWidth: chartBorderWidth,
            borderColor: '#ffffff',
          },
        };
      });
  }

  // 初始化图表
  function initOptionsData() {
    const { functionalCount, apiCaseCount, apiScenarioCount } = detail.value;
    functionCaseOptions.value.series.data = getPassRateData(functionalCount);
    apiCaseOptions.value.series.data = getPassRateData(apiCaseCount);
    scenarioCaseOptions.value.series.data = getPassRateData(apiScenarioCount);
  }

  const functionCasePassRate = computed(() => {
    const apiCaseDetail = getSummaryDetail(detail.value.functionalCount || defaultCount);
    return apiCaseDetail.successRate;
  });

  const apiCasePassRate = computed(() => {
    const apiCaseDetail = getSummaryDetail(detail.value.apiCaseCount || defaultCount);
    return apiCaseDetail.successRate;
  });

  const scenarioCasePassRate = computed(() => {
    const apiScenarioDetail = getSummaryDetail(detail.value.apiScenarioCount || defaultCount);
    return apiScenarioDetail.successRate;
  });
  const functionalCaseTotal = computed(() => getSummaryDetail(detail.value.functionalCount).caseTotal);
  const apiCaseTotal = computed(() => getSummaryDetail(detail.value.apiCaseCount).caseTotal);
  const scenarioCaseTotal = computed(() => getSummaryDetail(detail.value.apiScenarioCount).caseTotal);

  const reportAnalysisList = computed<ReportMetricsItemModel[]>(() => {
    if (isGroup.value) {
      return [
        {
          name: t('report.detail.testPlanTotal'),
          value: detail.value.planCount,
          unit: t('report.detail.number'),
          icon: 'plan_total',
        },
        {
          name: t('report.detail.testPlanCaseTotal'),
          value: detail.value.caseTotal,
          unit: t('report.detail.number'),
          icon: 'case_total',
        },
        {
          name: t('report.passRate'),
          value: detail.value.passRate,
          unit: '%',
          icon: 'passRate',
        },
        {
          name: t('report.detail.totalDefects'),
          value: addCommasToNumber(detail.value.bugCount),
          unit: t('report.detail.number'),
          icon: 'bugTotal',
        },
      ];
    }
    return [
      {
        name: t('report.detail.threshold'),
        value: detail.value.passThreshold,
        unit: '%',
        icon: 'threshold',
      },
      {
        name: t('report.passRate'),
        value: detail.value.passRate,
        unit: '%',
        icon: 'passRate',
      },
      {
        name: t('report.detail.performCompletion'),
        value: detail.value.executeRate,
        unit: '%',
        icon: 'passRate',
      },
      {
        name: t('report.detail.totalDefects'),
        value: addCommasToNumber(detail.value.bugCount),
        unit: t('report.detail.number'),
        icon: 'bugTotal',
      },
    ];
  });

  function showItem(item: configItem) {
    switch (item.value) {
      case ReportCardTypeEnum.FUNCTIONAL_DETAIL:
        return functionalCaseTotal.value > 0;
      case ReportCardTypeEnum.API_CASE_DETAIL:
        return apiCaseTotal.value > 0;
      case ReportCardTypeEnum.SCENARIO_CASE_DETAIL:
        return scenarioCaseTotal.value > 0;
      default:
        return true;
    }
  }

  const cardCount = computed(() => {
    const totalList = [functionalCaseTotal.value, apiCaseTotal.value, scenarioCaseTotal.value];
    let count = 2;
    totalList.forEach((item: number) => {
      if (item > 0) {
        count++;
      }
    });
    return count;
  });

  const originLayoutInfo = ref([]);

  async function getDefaultLayout() {
    try {
      const res = await getReportLayout(detail.value.id, shareId.value);
      const result = res.map((item: any) => {
        return {
          id: item.id,
          value: item.name,
          label: item.label,
          content: item.value || '',
          type: item.type,
          enableEdit: false,
          richTextTmpFileIds: item.richTextTmpFileIds,
        };
      });
      innerCardList.value = result;
      originLayoutInfo.value = cloneDeep(result);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const isDefaultLayout = ref<boolean>(false);

  // 获取内容详情
  function getContent(item: configItem): customValueForm {
    if (isDefaultLayout.value) {
      return {
        content: richText.value.summary || '',
        label: t(item.label),
        richTextTmpFileIds: [],
      };
    }
    return {
      content: item.content || '',
      label: t(item.label),
      richTextTmpFileIds: item.richTextTmpFileIds,
    };
  }

  const currentMode = ref<string>('drawer');

  /** 缺陷明细 */
  const bugColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      width: 80,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'status',
      width: 80,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUserName',
      width: 150,
    },
    {
      title: 'bugManagement.numberOfCase',
      dataIndex: 'relationCaseCount',
      width: 70,
    },
  ];
  const reportBugList = () => {
    return !shareId.value ? getReportBugList : getReportShareBugList;
  };
  const {
    propsRes: bugTableProps,
    loadList: loadBugList,
    setLoadListParams: setLoadBugListParams,
  } = useTable(reportBugList(), {
    scroll: { x: '100%', y: 'auto' },
    columns: bugColumns,
    showSelectorAll: false,
  });

  /** 用例明细 */
  const staticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      width: 80,
      ellipsis: true,
    },
    {
      title: 'case.caseName',
      dataIndex: 'name',
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      width: 80,
    },
  ];
  const lastStaticColumns: MsTableColumn = [
    {
      title: 'common.belongModule',
      dataIndex: 'moduleName',
      ellipsis: true,
      width: 200,
    },
    {
      title: 'case.caseLevel',
      dataIndex: 'priority',
      slotName: 'caseLevel',
      width: 80,
    },

    {
      title: 'testPlan.featureCase.executor',
      dataIndex: 'executeUser',
      width: 150,
    },
    {
      title: 'testPlan.featureCase.bugCount',
      dataIndex: 'bugCount',
      width: 70,
    },
  ];

  const testPlanNameColumns: MsTableColumn = [
    {
      title: 'report.plan.name',
      dataIndex: 'planName',
      width: 200,
    },
  ];

  const caseColumns = computed(() => {
    if (isGroup.value) {
      return [...staticColumns, ...testPlanNameColumns, ...lastStaticColumns];
    }
    return [...staticColumns, ...lastStaticColumns];
  });

  const reportFeatureCaseList = () => {
    return !shareId.value ? getReportFeatureCaseList : getReportShareFeatureCaseList;
  };
  const {
    propsRes: caseTableProps,
    loadList: loadCaseList,
    setLoadListParams: setLoadCaseListParams,
  } = useTable(reportFeatureCaseList(), {
    scroll: { x: '100%', y: 'auto' },
    columns: caseColumns.value,
    heightUsed: 20,
    showSelectorAll: false,
  });

  /** 接口/场景明细 */
  const apiStaticColumns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      width: 110,
    },
    {
      title: 'common.name',
      dataIndex: 'name',
    },
    {
      title: 'report.detail.level',
      dataIndex: 'priority',
      slotName: 'priority',
      width: 80,
    },
    {
      title: 'common.executionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      width: 80,
    },
  ];

  const apiColumns = computed(() => {
    if (isGroup.value) {
      return [...apiStaticColumns, ...testPlanNameColumns, ...lastStaticColumns];
    }
    return [...apiStaticColumns, ...lastStaticColumns];
  });

  const useApiTable = useTable(getApiPage, {
    scroll: { x: '100%', y: 'auto' },
    columns: apiColumns.value,
    showSelectorAll: false,
    showSetting: false,
  });
  const useScenarioTable = useTable(getScenarioPage, {
    scroll: { x: '100%', y: 'auto' },
    columns: apiColumns.value,
    showSelectorAll: false,
    showSetting: false,
  });

  async function getDetail() {
    try {
      loading.value = true;
      detail.value = await getReportDetail(reportId.value);
      const { defaultLayout, id, name, summary } = detail.value;
      isDefaultLayout.value = defaultLayout;
      richText.value.summary = summary;
      reportForm.value.reportName = name;
      initOptionsData();
      if (!defaultLayout && id) {
        getDefaultLayout();
      } else {
        innerCardList.value = isGroup.value ? cloneDeep(defaultGroupConfig) : cloneDeep(defaultSingleConfig);
      }
      setLoadBugListParams({ reportId: reportId.value, shareId: shareId.value ?? undefined, pageSize: 500 });
      setLoadCaseListParams({ reportId: reportId.value, shareId: shareId.value ?? undefined, startPager: false });
      useApiTable.setLoadListParams({
        reportId: reportId.value,
        shareId: shareId.value ?? undefined,
        pageSize: 500,
      });
      useScenarioTable.setLoadListParams({
        reportId: reportId.value,
        shareId: shareId.value ?? undefined,
        pageSize: 500,
      });
      await Promise.all([loadBugList(), loadCaseList(), useApiTable.loadList(), useScenarioTable.loadList()]);
      setTimeout(() => {
        nextTick(() => {
          exportPdf(detail.value.name, 'report-detail');
        });
      }, 0);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  onBeforeMount(() => {
    getDetail();
  });
</script>

<style lang="less" scoped>
  .report-header {
    @apply mb-4 flex items-center bg-white;

    padding: 16px;
    border-radius: var(--border-radius-large);
    box-shadow: 0 0 10px rgb(120 56 135 / 5%);
  }
  .block-title {
    @apply mb-4 font-medium;
  }
  .analysis-wrapper {
    @apply mb-4 grid items-center gap-4;
    .analysis {
      padding: 24px;
      height: 250px;
      border: 1px solid transparent;
      box-shadow: 0 0 10px rgba(120 56 135/ 5%);
      @apply rounded-xl bg-white;
      .charts {
        @apply absolute text-center;

        top: 50%;
        right: 0;
        bottom: 0;
        left: 50%;
        z-index: 99;
        width: 70px;
        height: 42px;
        transform: translateY(-50%) translateX(-50%);
      }
    }
    &[data-cards='2'],
    &[data-cards='4'] {
      grid-template-columns: repeat(2, 1fr);
    }
    &[data-cards='3'] {
      grid-template-columns: repeat(3, 1fr);
    }
    // 有5个的时候，上面2个，下面3个
    &[data-cards='5'] {
      grid-template-columns: repeat(6, 1fr);
      & > .analysis:nth-child(1),
      & > .analysis:nth-child(2) {
        grid-column: span 3;
      }
      & > .analysis:nth-child(n + 3) {
        grid-column: span 2;
      }
    }
  }
  .card-item {
    position: relative;
    width: 100%;
    border: 1px solid transparent;
    border-radius: 12px;
    .action {
      position: absolute;
      top: -14px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 9 !important;
      background: white;
      opacity: 0;
      @apply flex items-center justify-end;
      .actionList {
        padding: 4px;
        border-radius: 4px;
        @apply flex items-center justify-center;
      }
    }
    &:hover > .action {
      opacity: 1;
    }
    &:hover > .action > .actionList {
      color: rgb(var(--primary-5));
      box-shadow: 0 4px 10px -1px rgba(100 100 102/ 15%);
    }
  }
  .wrapper-preview-card {
    display: flex;
    padding: 16px;
    border-radius: 12px;
    box-shadow: 0 0 10px rgb(120 56 135 / 5%);
    @apply flex-col bg-white;
  }
  :deep(.arco-table-body) {
    max-height: 100% !important;
  }
</style>
