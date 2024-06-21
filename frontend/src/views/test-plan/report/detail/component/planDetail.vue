<template>
  <ReportHeader v-if="!props.isDrawer" :detail="detail" :share-id="shareId" :is-group="false" />
  <div class="analysis-wrapper" :data-cards="cardCount">
    <div class="analysis min-w-[238px]">
      <div class="block-title">{{ t('report.detail.api.reportAnalysis') }}</div>
      <ReportMetricsItem
        v-for="analysisItem in reportAnalysisList"
        :key="analysisItem.name"
        :item-info="analysisItem"
      />
    </div>
    <div class="analysis min-w-[410px]">
      <ExecuteAnalysis :detail="detail" />
    </div>
    <div v-if="functionalCaseTotal" class="analysis min-w-[330px]">
      <div class="block-title">{{ t('report.detail.useCaseAnalysis') }}</div>
      <div class="flex">
        <div class="w-[70%]">
          <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="pending" />
          <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="success" />
          <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="block" />
          <SingleStatusProgress :detail="detail" type="FUNCTIONAL" status="error" />
        </div>
        <div class="relative w-[30%] min-w-[150px]">
          <div class="charts absolute w-full text-center">
            <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
            <a-popover position="bottom" content-class="response-popover-content">
              <div class="flex justify-center text-[18px] font-medium">
                <div class="one-line-text max-w-[80px] text-[var(--color-text-1)]">{{ functionCasePassRate }} </div>
              </div>
              <template #content>
                <div class="min-w-[95px] max-w-[400px] p-4 text-[14px]">
                  <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                  <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">{{ functionCasePassRate }}</div>
                </div>
              </template>
            </a-popover>
          </div>
          <div class="flex h-full w-full min-w-[150px] items-center justify-center">
            <MsChart width="150px" height="150px" :options="functionCaseOptions"
          /></div>
        </div>
      </div>
    </div>
    <div v-if="apiCaseTotal" class="analysis min-w-[330px]">
      <div class="block-title">{{ t('report.detail.apiUseCaseAnalysis') }}</div>
      <div class="flex">
        <div class="w-[70%]">
          <SingleStatusProgress type="API" :detail="detail" status="pending" />
          <SingleStatusProgress type="API" :detail="detail" status="success" />
          <SingleStatusProgress type="API" :detail="detail" status="fakeError" />
          <SingleStatusProgress type="API" :detail="detail" status="error" />
        </div>
        <div class="relative w-[30%] min-w-[150px]">
          <div class="charts absolute w-full text-center">
            <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
            <a-popover position="bottom" content-class="response-popover-content">
              <div class="flex justify-center text-[18px] font-medium">
                <div class="one-line-text max-w-[80px] text-[var(--color-text-1)]">{{ apiCasePassRate }} </div>
              </div>
              <template #content>
                <div class="min-w-[95px] max-w-[400px] p-4 text-[14px]">
                  <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                  <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">{{ apiCasePassRate }}</div>
                </div>
              </template>
            </a-popover>
          </div>
          <div class="flex h-full w-full min-w-[150px] items-center justify-center">
            <MsChart width="150px" height="150px" :options="apiCaseOptions"
          /></div>
        </div>
      </div>
    </div>
    <div v-if="scenarioCaseTotal" class="analysis min-w-[330px]">
      <div class="block-title">{{ t('report.detail.scenarioUseCaseAnalysis') }}</div>
      <div class="flex">
        <div class="w-[70%]">
          <SingleStatusProgress type="SCENARIO" :detail="detail" status="pending" />
          <SingleStatusProgress type="SCENARIO" :detail="detail" status="success" />
          <SingleStatusProgress type="SCENARIO" :detail="detail" status="fakeError" />
          <SingleStatusProgress type="SCENARIO" :detail="detail" status="error" />
        </div>
        <div class="relative w-[30%] min-w-[150px]">
          <div class="charts absolute w-full text-center">
            <div class="text-[12px] !text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
            <a-popover position="bottom" content-class="response-popover-content">
              <div class="flex justify-center text-[18px] font-medium">
                <div class="one-line-text max-w-[80px] text-[var(--color-text-1)]">{{ scenarioCasePassRate }} </div>
              </div>
              <template #content>
                <div class="min-w-[95px] max-w-[400px] p-4 text-[14px]">
                  <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.passRate') }}</div>
                  <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">{{ scenarioCasePassRate }}</div>
                </div>
              </template>
            </a-popover>
          </div>
          <div class="flex h-full w-full min-w-[150px] items-center justify-center">
            <MsChart width="150px" height="150px" :options="scenarioCaseOptions"
          /></div>
        </div>
      </div>
    </div>
  </div>

  <Summary
    v-model:richText="richText"
    :share-id="shareId"
    :show-button="showButton"
    :is-plan-group="false"
    :detail="detail"
    @update-summary="handleUpdateReportDetail"
    @cancel="handleCancel"
    @handle-summary="handleSummary"
  />
  <MsCard simple auto-height auto-width>
    <MsTab
      v-model:active-key="activeTab"
      :show-badge="false"
      :content-tab-list="contentTabList"
      no-content
      class="relative mb-[16px] border-b"
    />
    <BugTable v-if="activeTab === 'bug'" :report-id="detail.id" :share-id="shareId" />
    <FeatureCaseTable
      v-if="activeTab === 'featureCase'"
      :active-tab="activeTab"
      :report-id="detail.id"
      :share-id="shareId"
    />
    <ApiAndScenarioTable
      v-if="['apiCase', 'scenarioCase'].includes(activeTab)"
      :report-id="detail.id"
      :share-id="shareId"
      :active-tab="activeTab"
    />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { useEventListener } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import ReportMetricsItem from './ReportMetricsItem.vue';
  import SingleStatusProgress from '@/views/test-plan/report/component/singleStatusProgress.vue';
  import ApiAndScenarioTable from '@/views/test-plan/report/detail/component/apiAndScenarioTable.vue';
  import BugTable from '@/views/test-plan/report/detail/component/bugTable.vue';
  import ExecuteAnalysis from '@/views/test-plan/report/detail/component/executeAnalysis.vue';
  import FeatureCaseTable from '@/views/test-plan/report/detail/component/featureCaseTable.vue';
  import ReportHeader from '@/views/test-plan/report/detail/component/reportHeader.vue';
  import Summary from '@/views/test-plan/report/detail/component/summary.vue';

  import { updateReportDetail } from '@/api/modules/test-plan/report';
  import { commonConfig, defaultCount, defaultReportDetail, seriesConfig, statusConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import type {
    countDetail,
    PlanReportDetail,
    ReportMetricsItemModel,
    StatusListType,
  } from '@/models/testPlan/testPlanReport';

  import { getSummaryDetail } from '@/views/test-plan/report/utils';

  const { t } = useI18n();

  const route = useRoute();
  const props = defineProps<{
    detailInfo: PlanReportDetail;
    isDrawer?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updateSuccess'): void;
  }>();

  const detail = ref<PlanReportDetail>({ ...cloneDeep(defaultReportDetail) });
  const showButton = ref<boolean>(false);
  const richText = ref<{ summary: string; richTextTmpFileIds?: string[] }>({
    summary: '',
  });

  /**
   * 分享share
   */
  const shareId = ref<string>(route.query.shareId as string);

  // 功能用例分析
  const functionCaseOptions = ref({
    ...commonConfig,
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
    return valueList.map((item: StatusListType) => {
      return {
        value: caseCountDetail[item.value] || 0,
        name: t(item.label),
        itemStyle: {
          color: success ? item.color : '#D4D4D8',
          borderWidth: 2,
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

  async function handleUpdateReportDetail() {
    try {
      await updateReportDetail({
        id: detail.value.id,
        summary: richText.value.summary,
        richTextTmpFileIds: richText.value.richTextTmpFileIds ?? [],
      });
      Message.success(t('common.updateSuccess'));
      showButton.value = false;
      emit('updateSuccess');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const reportAnalysisList = computed<ReportMetricsItemModel[]>(() => [
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
  ]);

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

  const getFunctionalTab = computed(() => {
    return functionalCaseTotal.value
      ? [
          {
            value: 'featureCase',
            label: t('report.detail.featureCaseDetails'),
          },
        ]
      : [];
  });

  const getApiTab = computed(() => {
    return apiCaseTotal.value
      ? [
          {
            value: 'apiCase',
            label: t('report.detail.apiCaseDetails'),
          },
        ]
      : [];
  });

  const getScenarioTab = computed(() => {
    return scenarioCaseTotal.value
      ? [
          {
            value: 'scenarioCase',
            label: t('report.detail.scenarioCaseDetails'),
          },
        ]
      : [];
  });

  const activeTab = ref('bug');
  const contentTabList = ref([
    {
      value: 'bug',
      label: t('report.detail.bugDetails'),
    },
    ...getFunctionalTab.value,
    ...getApiTab.value,
    ...getScenarioTab.value,
  ]);

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

  watchEffect(() => {
    if (props.detailInfo) {
      detail.value = cloneDeep(props.detailInfo);
      richText.value.summary = detail.value.summary;
      initOptionsData();
    }
  });

  onMounted(async () => {
    nextTick(() => {
      const editorContent = document.querySelector('.editor-content');
      useEventListener(editorContent, 'click', () => {
        showButton.value = true;
      });
    });
  });

  function handleCancel() {
    richText.value = { summary: detail.value.summary };
    showButton.value = false;
  }

  function handleSummary(content: string) {
    richText.value.summary = content;
  }
</script>

<style scoped lang="less">
  .block-title {
    @apply mb-4 font-medium;
  }
  .analysis-wrapper {
    @apply mb-4 grid items-center gap-4;
    .analysis {
      padding: 24px;
      height: 250px;
      box-shadow: 0 0 10px rgba(120 56 135/ 5%);
      @apply rounded-xl bg-white;
      .charts {
        top: 36%;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 99;
        margin: auto;
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
</style>
