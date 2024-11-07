<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.useCasesCount') }} </div>
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
          <PassRatePie :options="options" :size="60" :value-list="coverRateValueList" />
        </div>
      </div>
      <div class="mt-[16px]">
        <SetReportChart
          size="120px"
          gap="24"
          :legend-data="legendData"
          :options="executeCharOptions"
          :request-total="100000"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 关联用例数量
   */
  import { ref } from 'vue';

  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';
  import SetReportChart from '@/views/api-test/report/component/case/setReportChart.vue';

  import { commonConfig, seriesConfig, toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { LegendData } from '@/models/apiTest/report';

  const appStore = useAppStore();

  const { t } = useI18n();
  const projectIds = ref('');

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

  const coverRateValueList = ref([
    {
      label: t('workbench.homePage.covered'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.notCover'),
      value: 2000,
    },
  ]);

  const legendData = ref<LegendData[]>([
    {
      label: t('workbench.homePage.apiUseCases'),
      value: 'apiUseCases',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--success-6))] ml-[24px]',
    },
    {
      label: t('workbench.homePage.sceneUseCase'),
      value: 'sceneUseCase',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--link-6))] ml-[24px]',
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

<style scoped lang="less"></style>
