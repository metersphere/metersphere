<template>
  <div class="report-container">
    <!-- 报告参数开始 -->
    <ReportDetailHeader :detail="detail" show-type="API" />
    <!-- 报告参数结束 -->
    <!-- 报告分析，报告步骤分析和请求分析开始 -->
    <div class="analyze mb-1">
      <div class="analyze-item">
        <div class="block-title">{{ t('report.detail.api.reportAnalysis') }}</div>
        <ReportMetricsItem
          v-for="analysisItem in reportAnalysisList"
          :key="analysisItem.name"
          :item-info="analysisItem"
        />
      </div>
      <!-- 步骤分析 -->
      <div class="analyze-item request-analyze">
        <div class="block-title">{{ t('report.detail.api.stepAnalysis') }}</div>
        <SetReportChart
          :legend-data="stepAnalysisLegendData"
          :options="stepCharOptions"
          :request-total="getIndicators(detail.stepTotal) || 0"
        />
      </div>
      <!-- 请求分析 -->
      <div class="analyze-item request-analyze">
        <div class="block-title">{{ t('report.detail.api.requestAnalysis') }}</div>
        <SetReportChart
          :legend-data="legendData"
          :options="requestCharOptions"
          :request-total="getIndicators(detail.requestTotal) || 0"
        />
      </div>
    </div>
    <!-- 报告分析，报告步骤分析和请求分析结束 -->
    <!-- 报告明细开始 -->
    <div class="report-info">
      <reportInfoHeader
        v-model:keywordName="keywordName"
        v-model:keyword="cascaderKeywords"
        v-model:active-tab="activeTab"
        show-type="API"
        :is-export="props.isExport"
        @search="searchHandler"
        @reset="resetHandler"
      />
      <TiledList
        ref="tiledListRef"
        v-model:keyword-name="keywordName"
        :case-id="props.caseId"
        :case-name="props.caseName"
        :key-words="cascaderKeywords"
        show-type="API"
        :get-report-step-detail="props.getReportStepDetail"
        :active-type="activeTab"
        :report-detail="detail || []"
        :is-export="props.isExport"
        :is-filter-step="props.isFilterStep"
        class="p-[16px]"
      />
    </div>
    <!-- 报告明细结束 -->
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import SetReportChart from './case/setReportChart.vue';
  import ReportDetailHeader from './reportDetailHeader.vue';
  import reportInfoHeader from './step/reportInfoHeaders.vue';
  import TiledList from './tiledList.vue';
  import ReportMetricsItem from '@/views/test-plan/report/detail/component/system-card/ReportMetricsItem.vue';

  import getVisualThemeColor from '@/config/chartTheme';
  import { toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { formatDuration } from '@/utils';

  import type { LegendData, ReportDetail } from '@/models/apiTest/report';
  import type { ReportMetricsItemModel } from '@/models/testPlan/testPlanReport';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  import { getIndicators } from '../utils';

  const { t } = useI18n();
  const props = defineProps<{
    detailInfo?: ReportDetail;
    getReportStepDetail?: (...args: any) => Promise<any>; // 获取步骤的详情内容接口
    isExport?: boolean; // 是否是导出pdf预览
    isFilterStep?: boolean; // 是否打开抽屉之前过滤用例步骤
    caseName?: string; // 用例名称关键字
    caseId?: string; // 用例id
  }>();

  const detail = ref<ReportDetail>({
    id: '',
    name: '', // 报告名称
    testPlanId: '',
    createUser: '',
    deleteTime: 0,
    deleteUser: '',
    deleted: false,
    updateUser: '',
    updateTime: 0,
    startTime: 0, // 开始时间/同创建时间一致
    endTime: 0, //  结束时间/报告执行完成
    requestDuration: 0, // 请求总耗时
    status: ExecuteStatusEnum.PENDING, // 报告状态/SUCCESS/ERROR
    triggerMode: '', // 触发方式
    runMode: '', // 执行模式
    poolId: '', // 资源池
    poolName: '', // 资源池名称
    versionId: '',
    integrated: false, // 是否是集成报告
    projectId: '',
    environmentId: '', // 环境id
    environmentName: '', // 环境名称
    errorCount: 0, // 失败数
    fakeErrorCount: 0, // 误报数
    pendingCount: 0, // 未执行数
    successCount: 0, // 成功数
    assertionCount: 0, // 总断言数
    assertionSuccessCount: 0, // 成功断言数
    requestErrorRate: '', // 请求失败率
    requestPendingRate: '', // 请求未执行率
    requestFakeErrorRate: '', // 请求误报率
    requestPassRate: '', // 请求通过率
    assertionPassRate: '', // 断言通过率
    scriptIdentifier: '', // 脚本标识
    children: [], // 步骤列表
    stepTotal: 0, // 步骤总数
    console: '',
    stepSuccessCount: 0, // 步骤成功数
    stepErrorCount: 0, // 步骤失败数
    stepFakeErrorCount: 0, // 步骤误报数
    stepPendingCount: 0, // 步骤未执行数
  });

  const cascaderKeywords = ref<string>('');

  const getTotalTime = computed(() => {
    if (detail.value) {
      const { endTime, startTime } = detail.value;
      if (endTime && startTime && endTime !== 0 && startTime !== 0) {
        return formatDuration(endTime - startTime);
      }
      return '0';
    }
    return '0';
  });

  const getRunMode = computed(() => {
    if (detail.value.integrated) {
      return detail.value.runMode === 'SERIAL' ? t('case.execute.serial') : t('case.execute.parallel');
    }
    return '';
  });
  const keywordName = ref<string>(props.caseName || '');

  const reportAnalysisList = computed<ReportMetricsItemModel[]>(() => [
    {
      name: t('report.detail.api.totalTime'),
      value: getTotalTime.value.split('-')[0],
      unit: getTotalTime.value.split('-')[1] || 'ms',
      icon: 'totalTime',
      tip: t('report.detail.api.totalTimeTip'),
      runMode: getRunMode.value,
    },
    {
      name: t('report.detail.api.requestTotalTime'),
      value: detail.value.requestDuration !== null ? formatDuration(detail.value.requestDuration).split('-')[0] : '0',
      unit: detail.value.requestDuration !== null ? formatDuration(detail.value.requestDuration).split('-')[1] : 'ms',
      icon: 'totalTime',
      tip: t('report.detail.api.requestTotalTimeTip'),
    },
    {
      name: t('report.detail.api.assertPass'),
      value: getIndicators(detail.value.assertionPassRate),
      unit: '%',
      icon: 'passRate',
    },
  ]);

  const legendData = ref<LegendData[]>([]);
  const stepAnalysisLegendData = ref<LegendData[]>([]);
  const defaultCharOptions = {
    tooltip: {
      ...toolTipConfig,
    },
    legend: {
      show: false,
    },
    animation: !props.isExport, // pdf预览需要关闭渲染动画
    series: {
      name: '',
      type: 'pie',
      radius: ['62%', '80%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center',
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 40,
          fontWeight: 'bold',
        },
      },
      labelLine: {
        show: false,
      },
      data: [
        {
          value: 0,
          name: t('common.pass'),
          itemStyle: {
            color: '#00C261',
          },
        },
        {
          value: 0,
          name: t('common.fakeError'),
          itemStyle: {
            color: '#FFC14E',
          },
        },
        {
          value: 0,
          name: t('common.fail'),
          itemStyle: {
            color: '#ED0303',
          },
        },
        {
          value: 0,
          name: t('common.unExecute'),
          itemStyle: {
            color: '#D4D4D8',
          },
        },
      ],
    },
  };
  const stepCharOptions = ref(cloneDeep(defaultCharOptions));
  const requestCharOptions = ref(cloneDeep(defaultCharOptions));
  const activeTab = ref<'tiled' | 'tab'>('tiled');

  function getRote(count: number, countTotal: number) {
    if (Number.isNaN(count) || Number.isNaN(countTotal) || countTotal === 0) {
      return '0.00';
    }
    return (((count || 0) / countTotal) * 100).toFixed(2);
  }
  function initOptionsData() {
    const tempArr = [
      {
        label: 'common.pass',
        value: 'successCount',
        color: '#00C261',
        class: 'bg-[rgb(var(--success-6))]',
        rateKey: 'requestPassRate',
      },
      {
        label: 'common.fakeError',
        value: 'fakeErrorCount',
        color: '#FFC14E',
        class: 'bg-[rgb(var(--warning-6))]',
        rateKey: 'requestFakeErrorRate',
      },
      {
        label: 'common.fail',
        value: 'errorCount',
        color: '#ED0303',
        class: 'bg-[rgb(var(--danger-6))]',
        rateKey: 'requestErrorRate',
      },
      {
        label: 'common.unExecute',
        value: 'pendingCount',
        color: '#D4D4D8',
        class: 'bg-[var(--color-text-input-border)]',
        rateKey: 'requestPendingRate',
      },
    ];
    const requestChartBorderWidth = tempArr.filter((e) => Number(detail.value[e.value]) > 0).length === 1 ? 0 : 2;
    requestCharOptions.value.series.data = tempArr.map((item: any) => {
      return {
        value: detail.value[item.value] || 0,
        name: t(item.label),
        itemStyle: {
          color: item.color,
          borderWidth: requestChartBorderWidth,
          borderColor: getVisualThemeColor('itemStyleBorderColor'),
        },
      };
    });
    legendData.value = tempArr.map((item: any) => {
      return {
        ...item,
        label: t(item.label),
        count: detail.value[item.value] || 0,
        rote: `${detail.value[item.rateKey] || 0}%`,
      };
    });
    const stepChartBorderWidth =
      tempArr.filter((e) => Number(detail.value[`step${e.value.charAt(0).toUpperCase() + e.value.slice(1)}`]) > 0)
        .length === 1
        ? 0
        : 2;
    stepCharOptions.value.series.data = tempArr.map((item: any) => {
      const valueName = `step${item.value.charAt(0).toUpperCase() + item.value.slice(1)}`;
      return {
        value: detail.value[valueName] || 0,
        name: t(item.label),
        itemStyle: {
          color: item.color,
          borderWidth: stepChartBorderWidth,
          borderColor: getVisualThemeColor('itemStyleBorderColor'),
        },
      };
    });
    stepAnalysisLegendData.value = tempArr.map((item: any) => {
      const valueName = `step${item.value.charAt(0).toUpperCase() + item.value.slice(1)}`;
      return {
        ...item,
        label: t(item.label),
        count: detail.value[valueName] || 0,
        rote: `${getRote(detail.value[valueName], detail.value.stepTotal)}%`,
      };
    });
  }

  const tiledListRef = ref<InstanceType<typeof TiledList>>();
  function searchHandler() {
    if (keywordName.value) {
      tiledListRef.value?.updateDebouncedSearch();
    } else {
      tiledListRef.value?.initStepTree();
    }
  }

  function resetHandler() {
    tiledListRef.value?.initStepTree();
  }

  watchEffect(() => {
    if (props.detailInfo) {
      detail.value = props.detailInfo;
      initOptionsData();
    }
  });
</script>

<style scoped lang="less">
  .report-container {
    background: var(--color-text-n9);
    .report-header {
      padding: 0 16px;
      height: 54px;
      border-radius: 4px;
      background: var(--color-text-fff);
      @apply mb-4;

      background-color: var(--color-text-fff);
    }
    .analyze {
      height: 196px;
      border-radius: 4px;
      @apply mb-4 flex justify-between;
      .analyze-item {
        padding: 16px;
        width: 33%;
        border-radius: 4px;
        @apply h-full;

        background-color: var(--color-text-fff);
      }
      .request-analyze {
        @apply ml-4 flex-grow;
        .chart-legend {
          .chart-legend-item {
            @apply grid grid-cols-3;
          }
          .chart-flag {
            @apply flex items-center;
            .count {
              color: var(--color-text-1);
            }
          }
        }
      }
    }
    .report-info {
      padding: 16px;
      border-radius: 4px;
      background-color: var(--color-text-fff);
    }
  }
  .block-title {
    @apply mb-4 font-medium;
  }
  .charts {
    top: 30%;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 99;
    margin: auto;
  }
</style>
