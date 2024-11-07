<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.useCasesNumber') }} </div>
      <div>
        <MsSelect
          v-model:model-value="projectIds"
          :options="appStore.projectList"
          allow-clear
          allow-search
          value-key="id"
          label-key="name"
          :search-keys="['name']"
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
        >
        </MsSelect>
      </div>
    </div>
    <div class="my-[16px]">
      <div class="case-count-wrapper">
        <div class="case-count-item">
          <PassRatePie :options="options" :size="60" :value-list="reviewValueList" />
        </div>
        <div class="case-count-item">
          <PassRatePie :options="options" :size="60" :value-list="passValueList" />
        </div>
      </div>
      <div class="mt-[16px]">
        <SetReportChart size="120px" :legend-data="legendData" :options="executeCharOptions" :request-total="100000" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 用例数量
   */
  import { ref } from 'vue';

  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';
  import SetReportChart from '@/views/api-test/report/component/case/setReportChart.vue';

  import { commonConfig, seriesConfig, toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { LegendData } from '@/models/apiTest/report';

  const projectIds = ref('');
  const appStore = useAppStore();

  const { t } = useI18n();

  const options = ref({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
    legend: {
      show: false,
    },
    series: {
      name: '',
      type: 'pie',
      radius: ['80%', '100%'],
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
      data: [],
    },
  });

  const reviewValueList = ref([
    {
      label: t('workbench.homePage.reviewed'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.unReviewed'),
      value: 2000,
    },
  ]);
  const passValueList = ref([
    {
      label: t('workbench.homePage.havePassed'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.notPass'),
      value: 2000,
    },
  ]);

  const legendData = ref<LegendData[]>([
    {
      label: 'P0',
      value: 'P0',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--danger-6))] ml-[24px]',
    },
    {
      label: 'P1',
      value: 'P1',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--warning-6))] ml-[24px]',
    },
    {
      label: 'P2',
      value: 'P2',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--link-6))] ml-[24px]',
    },
    {
      label: 'P3',
      value: 'P3',
      rote: 30,
      count: 3,
      class: 'bg-[var(--color-text-input-border)] ml-[24px]',
    },
  ]);

  // 执行分析
  const executeCharOptions = ref({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
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
</script>

<style scoped lang="less">
  .card-wrapper {
    margin: 16px 0;
    padding: 24px;
    box-shadow: 0 0 10px rgba(120 56 135/ 5%);
    @apply rounded-xl bg-white;
    .title {
      font-size: 16px;
      @apply font-medium;
    }
    .case-count-wrapper {
      @apply flex items-center gap-4;
      .case-count-item {
        @apply flex-1;
      }
    }
  }
</style>
