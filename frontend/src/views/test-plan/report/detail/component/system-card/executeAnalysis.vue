<template>
  <div v-if="!props.hideTitle" class="mb-[16px] font-medium">{{ t('report.detail.executionAnalysis') }}</div>
  <SetReportChart
    size="130px"
    :legend-data="legendData"
    :options="executeCharOptions"
    :request-total="getIndicators(getTotal) || 0"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import SetReportChart from '@/views/api-test/report/component/case/setReportChart.vue';

  import getVisualThemeColor from '@/config/chartTheme';
  import { commonConfig, seriesConfig, statusConfig, toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { LegendData } from '@/models/apiTest/report';
  import type { PlanReportDetail, StatusListType } from '@/models/testPlan/testPlanReport';

  import { getIndicators } from '@/views/api-test/report/utils';

  const { t } = useI18n();

  const appStore = useAppStore();
  const props = defineProps<{
    detail: PlanReportDetail;
    hideTitle?: boolean;
    animation?: boolean; // 是否开启动画
    noBlock?: boolean;
  }>();

  const legendData = ref<LegendData[]>([]);

  // 执行分析
  const executeCharOptions = ref<Record<string, any>>({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
    animation: props.animation === undefined ? true : props.animation, // 关闭渲染动画
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
        {
          value: 0,
          name: t('common.block'),
          itemStyle: {
            color: '#B379C8',
          },
        },
      ],
    },
  });

  if (props.noBlock) {
    executeCharOptions.value.series.data.pop();
  }

  function initExecuteOptions() {
    const pieBorderWidth =
      statusConfig.filter((e) => Number(props.detail.executeCount[e.value]) > 0).length === 1 ? 0 : 2;

    const lastExecuteData: Record<string, any>[] = statusConfig
      .filter((item) => props.detail.executeCount[item.value] > 0)
      .map((item: StatusListType) => {
        const color = item.color === '#D4D4D8' ? getVisualThemeColor('initItemStyleColor') : item.color;
        const itemColor = props.animation ? color : item.color;

        return {
          value: props.detail.executeCount[item.value] || 0,
          name: t(item.label),
          itemStyle: {
            color: itemColor,
            borderWidth: pieBorderWidth,
            borderColor: props.animation ? getVisualThemeColor('itemStyleBorderColor') : '#ffffff',
          },
        };
      });

    executeCharOptions.value.series.data = lastExecuteData.length
      ? lastExecuteData
      : [
          {
            value: 1,
            tooltip: {
              show: false,
            },
            itemStyle: {
              color: props.animation ? getVisualThemeColor('initItemStyleColor') : '#D4D4D8',
            },
          },
        ];

    legendData.value = statusConfig.map((item: StatusListType) => {
      const rate = (props.detail.executeCount[item.value] / props.detail.caseTotal) * 100;
      return {
        ...item,
        label: t(item.label),
        count: props.detail.executeCount[item.value] || 0,
        rote: `${Number.isNaN(rate) ? 0 : rate.toFixed(2)}%`,
      };
    }) as unknown as LegendData[];
  }

  const getTotal = computed(() => {
    const { executeCount } = props.detail;
    if (executeCount) {
      const { success, error, fakeError, pending, block } = executeCount;
      return success + error + fakeError + pending + block;
    }
    return 0;
  });

  watchEffect(() => {
    if (Object.keys(props.detail).length > 0 || appStore.isDarkTheme) {
      initExecuteOptions();
    }
  });
</script>

<style scoped></style>
