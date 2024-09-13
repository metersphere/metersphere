<template>
  <a-spin :loading="loading" :tip="t('report.detail.exportingPdf')" class="report-detail-container">
    <div id="report-detail" class="report-detail">
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
        <div v-for="item of innerCardList" :id="`${item.value}`" :key="item.id" class="card-item mt-[16px]">
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
            <div v-else-if="item.value === ReportCardTypeEnum.CUSTOM_CARD" v-html="item.content"></div>
            <div v-else-if="item.value === ReportCardTypeEnum.SUMMARY" v-html="detail.summary"></div>
          </div>
        </div>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsChart from '@/components/pure/chart/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import { lastExecuteResultMap } from '@/components/business/ms-case-associate/utils';
  import { IconType } from '@/views/api-test/report/component/reportStatus.vue';
  import SingleStatusProgress from '@/views/test-plan/report/component/singleStatusProgress.vue';
  import ExecuteAnalysis from '@/views/test-plan/report/detail/component/system-card/executeAnalysis.vue';
  import ReportDetailTable from '@/views/test-plan/report/detail/component/system-card/reportDetailTable.vue';
  import ReportMetricsItem from '@/views/test-plan/report/detail/component/system-card/ReportMetricsItem.vue';

  import {
    getApiPage,
    getReportBugList,
    getReportDetail,
    getReportDetailPage,
    getReportDetailSharePage,
    getReportFeatureCaseList,
    getReportLayout,
    getReportShareBugList,
    getReportShareFeatureCaseList,
    getScenarioPage,
    logTestPlanReportExport,
  } from '@/api/modules/test-plan/report';
  import {
    commonConfig,
    defaultCount,
    defaultReportDetail,
    seriesConfig,
    statusConfig,
    toolTipConfig,
  } from '@/config/testPlan';
  import exportPDF, { PAGE_PDF_WIDTH_RATIO, PdfTableConfig } from '@/hooks/useExportPDF';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import type {
    configItem,
    countDetail,
    PlanReportDetail,
    ReportMetricsItemModel,
    StatusListType,
  } from '@/models/testPlan/testPlanReport';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  import { defaultGroupConfig, defaultSingleConfig, iconTypeStatus } from './component/reportConfig';
  import { getSummaryDetail } from '@/views/test-plan/report/utils';
  import { ColumnInput, RowInput } from 'jspdf-autotable';

  const { t } = useI18n();
  const route = useRoute();

  const innerCardList = ref<configItem[]>([]);

  const detail = ref<PlanReportDetail>({ ...cloneDeep(defaultReportDetail) });
  const reportId = ref<string>(route.query.id as string);
  const isGroup = computed(() => route.query.type === 'GROUP');
  const loading = ref<boolean>(true);
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
  const currentMode = ref<string>('drawer');

  async function getDefaultLayout() {
    try {
      const res = await getReportLayout(detail.value.id, shareId.value);
      innerCardList.value = res
        .filter((e: any) => [ReportCardTypeEnum.CUSTOM_CARD, ReportCardTypeEnum.SUMMARY].includes(e.value))
        .map((item: any) => {
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
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const isDefaultLayout = ref<boolean>(false);

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
      width: 100,
    },
  ];

  const apiColumns = computed(() => {
    if (isGroup.value) {
      return [
        ...apiStaticColumns,
        ...testPlanNameColumns,
        ...lastStaticColumns.filter((e) => e.dataIndex !== 'priority'),
      ];
    }
    return [...apiStaticColumns, ...lastStaticColumns.filter((e) => e.dataIndex !== 'priority')];
  });

  const fullCaseList = ref<any>([]);
  async function initCaseList() {
    fullCaseList.value = (
      await reportFeatureCaseList()({
        current: 1,
        pageSize: 500,
        reportId: reportId.value,
        shareId: shareId.value ?? undefined,
        startPager: false,
      })
    ).list;
  }

  const fullBugList = ref<any>([]);
  async function initBugList() {
    fullBugList.value = (
      await reportBugList()({
        current: 1,
        pageSize: 500,
        reportId: reportId.value,
        shareId: shareId.value ?? undefined,
        startPager: false,
      })
    ).list;
  }

  const fullApiList = ref<any>([]);
  async function initApiList() {
    fullApiList.value = (
      await getApiPage({
        current: 1,
        pageSize: 500,
        reportId: reportId.value,
        shareId: shareId.value ?? undefined,
        startPager: false,
      })
    ).list;
  }

  const fullScenarioList = ref<any>([]);
  async function initScenarioList() {
    fullScenarioList.value = (
      await getScenarioPage({
        current: 1,
        pageSize: 500,
        reportId: reportId.value,
        shareId: shareId.value ?? undefined,
        startPager: false,
      })
    ).list;
  }

  const groupColumns: MsTableColumn = [
    {
      title: 'report.plan.name',
      dataIndex: 'testPlanName',
      width: 180,
    },
    {
      title: 'report.detail.testPlanGroup.result',
      dataIndex: 'resultStatus',
      width: 200,
    },
    {
      title: 'report.detail.threshold',
      dataIndex: 'passThreshold',
      width: 150,
    },
    {
      title: 'report.passRate',
      dataIndex: 'passRate',
      width: 150,
    },
    {
      title: 'report.detail.testPlanGroup.useCasesCount',
      dataIndex: 'caseTotal',
      width: 100,
    },
  ];

  const reportDetailList = () => {
    return !shareId.value ? getReportDetailPage : getReportDetailSharePage;
  };
  const fullGroupList = ref<PlanReportDetail[]>([]);
  async function initGroupList() {
    fullGroupList.value = (
      await reportDetailList()({
        current: 1,
        pageSize: 500,
        reportId: reportId.value,
        shareId: shareId.value ?? undefined,
        startPager: false,
      })
    ).list;
  }
  function getExecutionResult(status?: string): IconType {
    return status && iconTypeStatus[status] ? iconTypeStatus[status] : iconTypeStatus.DEFAULT;
  }

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
        await getDefaultLayout();
        await initGroupList();
        nextTick(async () => {
          exportPDF(
            name,
            'report-detail',
            [
              {
                columnStyles: {
                  testPlanName: { cellWidth: 710 / PAGE_PDF_WIDTH_RATIO },
                  resultStatus: { cellWidth: 130 / PAGE_PDF_WIDTH_RATIO },
                  passThreshold: { cellWidth: 130 / PAGE_PDF_WIDTH_RATIO },
                  passRate: { cellWidth: 130 / PAGE_PDF_WIDTH_RATIO },
                  caseTotal: { cellWidth: 90 / PAGE_PDF_WIDTH_RATIO },
                },
                columns: groupColumns.map((item) => ({
                  ...item,
                  title: t(item.title as string),
                  dataKey: item.dataIndex,
                })) as ColumnInput[],
                body: fullGroupList.value.map((e) => ({
                  ...e,
                  resultStatus: t(getExecutionResult(e.resultStatus).label),
                  passRate: `${e.passRate}%`,
                  passThreshold: `${e.passThreshold}%`,
                })) as RowInput[],
              },
            ],
            () => {
              loading.value = false;
              Message.success(t('report.detail.exportPdfSuccess'));
            }
          );
        });
      } else {
        innerCardList.value = (isGroup.value ? cloneDeep(defaultGroupConfig) : cloneDeep(defaultSingleConfig)).filter(
          (e: any) => [ReportCardTypeEnum.CUSTOM_CARD, ReportCardTypeEnum.SUMMARY].includes(e.value)
        );
        await Promise.all([initBugList(), initCaseList(), initApiList(), initScenarioList()]);
        const tableArr: PdfTableConfig[] = [];
        if (fullBugList.value.length > 0) {
          tableArr.push({
            columnStyles: {
              num: { cellWidth: 120 / PAGE_PDF_WIDTH_RATIO },
              title: { cellWidth: 600 / PAGE_PDF_WIDTH_RATIO },
              status: { cellWidth: 110 / PAGE_PDF_WIDTH_RATIO },
              handleUserName: { cellWidth: 270 / PAGE_PDF_WIDTH_RATIO },
              relationCaseCount: { cellWidth: 90 / PAGE_PDF_WIDTH_RATIO },
            },
            columns: bugColumns.map((item) => ({
              ...item,
              title: t(item.title as string),
              dataKey: item.dataIndex,
            })) as ColumnInput[],
            body: fullBugList.value,
          });
        }
        if (fullCaseList.value.length > 0) {
          tableArr.push({
            columnStyles: {
              num: { cellWidth: 100 / PAGE_PDF_WIDTH_RATIO },
              name: { cellWidth: 480 / PAGE_PDF_WIDTH_RATIO },
              executeResult: { cellWidth: 110 / PAGE_PDF_WIDTH_RATIO },
              priority: { cellWidth: 110 / PAGE_PDF_WIDTH_RATIO },
              moduleName: { cellWidth: 200 / PAGE_PDF_WIDTH_RATIO },
              executeUser: { cellWidth: 100 / PAGE_PDF_WIDTH_RATIO },
              relationCaseCount: { cellWidth: 90 / PAGE_PDF_WIDTH_RATIO },
            },
            columns: caseColumns.value.map((item) => ({
              ...item,
              title: t(item.title as string),
              dataKey: item.dataIndex,
            })) as ColumnInput[],
            body: fullCaseList.value.map((e: any) => ({
              ...e,
              executeResult: t(lastExecuteResultMap[e.executeResult]?.statusText || '-'),
              executeUser: e.executeUser?.name || '-',
            })) as RowInput[],
          });
        }
        if (fullApiList.value.length > 0) {
          tableArr.push({
            columnStyles: {
              num: { cellWidth: 130 / PAGE_PDF_WIDTH_RATIO },
              name: { cellWidth: 450 / PAGE_PDF_WIDTH_RATIO },
              executeResult: { cellWidth: 110 / PAGE_PDF_WIDTH_RATIO },
              priority: { cellWidth: 80 / PAGE_PDF_WIDTH_RATIO },
              moduleName: { cellWidth: 200 / PAGE_PDF_WIDTH_RATIO },
              executeUser: { cellWidth: 130 / PAGE_PDF_WIDTH_RATIO },
              bugCount: { cellWidth: 90 / PAGE_PDF_WIDTH_RATIO },
            },
            columns: apiColumns.value.map((item) => ({
              ...item,
              title: t(item.title as string),
              dataKey: item.dataIndex,
            })) as ColumnInput[],
            body: fullApiList.value.map((e: any) => ({
              ...e,
              executeResult: t(lastExecuteResultMap[e.executeResult]?.statusText || '-'),
              executeUser: e.executeUser?.name || '-',
            })) as RowInput[],
          });
        }
        if (apiColumns.value.length > 0) {
          tableArr.push({
            columnStyles: {
              num: { cellWidth: 100 / PAGE_PDF_WIDTH_RATIO },
              name: { cellWidth: 480 / PAGE_PDF_WIDTH_RATIO },
              executeResult: { cellWidth: 110 / PAGE_PDF_WIDTH_RATIO },
              priority: { cellWidth: 80 / PAGE_PDF_WIDTH_RATIO },
              moduleName: { cellWidth: 200 / PAGE_PDF_WIDTH_RATIO },
              executeUser: { cellWidth: 130 / PAGE_PDF_WIDTH_RATIO },
              bugCount: { cellWidth: 90 / PAGE_PDF_WIDTH_RATIO },
            },
            columns: apiColumns.value.map((item) => ({
              ...item,
              title: t(item.title as string),
              dataKey: item.dataIndex,
            })) as ColumnInput[],
            body: fullScenarioList.value.map((e: any) => ({
              ...e,
              executeResult: t(lastExecuteResultMap[e.executeResult]?.statusText || '-'),
              executeUser: e.executeUser?.name || '-',
            })) as RowInput[],
          });
        }
        nextTick(async () => {
          exportPDF(name, 'report-detail', tableArr, () => {
            loading.value = false;
            Message.success(t('report.detail.exportPdfSuccess'));
          });
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      loading.value = false;
    }
  }

  async function logExport() {
    await logTestPlanReportExport(route.query.id as string);
  }

  onBeforeMount(() => {
    getDetail();
    logExport();
  });
</script>

<style lang="less">
  .arco-spin-mask-icon {
    @apply !fixed;
  }
</style>

<style lang="less" scoped>
  .report-detail-container {
    @apply flex justify-center;
    .report-detail {
      @apply overflow-x-auto;

      padding: 16px;
      width: 1190px;
      .ms-scroll-bar();
    }
  }
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
  :deep(#ms-table-footer-wrapper) {
    @apply hidden;
  }
</style>
