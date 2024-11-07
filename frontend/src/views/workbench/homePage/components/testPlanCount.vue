<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title">
        {{ t('workbench.homePage.numberOfTestPlan') }}
      </div>
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
    <div class="mt-[16px]">
      <TabCard :content-tab-list="testPlanTabList" not-has-padding hidden-border>
        <template #item="{ item }">
          <div class="w-full">
            <PassRatePie :options="options" :size="60" :value-list="item.valueList" />
          </div>
        </template>
      </TabCard>
      <div class="mt-[16px]">
        <SetReportChart size="120px" :legend-data="legendData" :options="testPlanCharOptions" :request-total="100000" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 测试计划数量
   */
  import { ref } from 'vue';

  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';
  import TabCard from './tabCard.vue';
  import SetReportChart from '@/views/api-test/report/component/case/setReportChart.vue';

  import { commonConfig, seriesConfig, toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { LegendData } from '@/models/apiTest/report';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const { t } = useI18n();
  const appStore = useAppStore();

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
  // 执行率
  const executionValueList = ref([
    {
      label: t('common.unExecute'),
      value: 10000,
    },
    {
      label: t('common.executed'),
      value: 2000,
    },
  ]);
  // 通过率
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

  // 完成率
  const completeValueList = ref([
    {
      label: t('common.completed'),
      value: 10000,
    },
    {
      label: t('common.inProgress'),
      value: 2000,
    },
    {
      label: t('workbench.homePage.unFinish'),
      value: 2000,
    },
  ]);
  const testPlanTabList = computed(() => {
    return [
      {
        label: '',
        value: 'execution',
        valueList: executionValueList.value,
        options,
      },
      {
        label: '',
        value: 'pass',
        valueList: passValueList.value,
        options,
      },
      {
        label: '',
        value: 'complete',
        valueList: completeValueList.value,
        options,
      },
    ];
  });

  const testPlanCharOptions = ref({
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

  const legendData = ref<LegendData[]>([
    {
      label: t('common.notStarted'),
      value: 'notStarted',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--primary-4))] ml-[24px]',
    },
    {
      label: t('common.inProgress'),
      value: 'inProgress',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--link-6))] ml-[24px]',
    },
    {
      label: t('common.completed'),
      value: 'completed',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--success-6))] ml-[24px]',
    },
    {
      label: t('common.archived'),
      value: 'archived',
      rote: 30,
      count: 3,
      class: 'bg-[var(--color-text-input-border)] ml-[24px]',
    },
  ]);
</script>

<style scoped lang="less">
  :deep(.arco-tabs-tab) {
    padding: 0 !important;
  }
</style>
