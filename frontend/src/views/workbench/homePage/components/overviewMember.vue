<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.staffOverview') }} </div>
      <div class="flex items-center gap-[8px]">
        <MsSelect
          v-model:model-value="projectIds"
          :options="projectOptions"
          :allow-search="false"
          allow-clear
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
          :multiple="true"
          :has-all-select="true"
          :default-all-select="true"
        >
        </MsSelect>
        <MsSelect
          v-model:model-value="memberIds"
          :options="memberOptions"
          :allow-search="false"
          allow-clear
          class="!w-[240px]"
          :prefix="t('workbench.homePage.staff')"
          :multiple="true"
          :has-all-select="true"
          :default-all-select="true"
        >
        </MsSelect>
      </div>
    </div>
    <div class="my-[16px]">
      <TabCard :content-tab-list="contentTabList" />
    </div>
    <!-- 概览图 -->
    <div>
      <MsChart height="300px" :options="options" />
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 人员概览
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import TabCard from './tabCard.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import { WorkOverviewEnum, WorkOverviewIconEnum } from '@/enums/workbenchEnum';

  import { commonColorConfig } from '../utils';
  import type { SelectOptionData } from '@arco-design/web-vue';

  const { t } = useI18n();

  const memberIds = ref('');
  const projectIds = ref('');

  const memberOptions = ref<SelectOptionData[]>([]);
  const projectOptions = ref<SelectOptionData[]>([]);

  const contentTabList = ref([
    {
      label: t('workbench.homePage.functionalUseCase'),
      value: WorkOverviewEnum.FUNCTIONAL,
      icon: WorkOverviewIconEnum.FUNCTIONAL,
      color: 'rgb(var(--primary-5))',
      count: 1000000,
    },
    {
      label: t('workbench.homePage.useCaseReview'),
      value: WorkOverviewEnum.CASE_REVIEW,
      icon: WorkOverviewIconEnum.CASE_REVIEW,
      color: 'rgb(var(--success-6))',
      count: 1000000,
    },
    {
      label: t('workbench.homePage.interfaceAPI'),
      value: WorkOverviewEnum.API,
      icon: WorkOverviewIconEnum.API,
      color: 'rgb(var(--link-6))',
      count: 1000000,
    },
    {
      label: t('workbench.homePage.interfaceCASE'),
      value: WorkOverviewEnum.API_CASE,
      icon: WorkOverviewIconEnum.API_CASE,
      color: 'rgb(var(--link-6))',
      count: 1000000,
    },
    {
      label: t('workbench.homePage.interfaceScenario'),
      value: WorkOverviewEnum.API_SCENARIO,
      icon: WorkOverviewIconEnum.API_SCENARIO,
      color: 'rgb(var(--link-6))',
      count: 1000000,
    },
    {
      label: t('workbench.homePage.apiPlan'),
      value: WorkOverviewEnum.API_PLAN,
      icon: WorkOverviewIconEnum.API_PLAN,
      color: 'rgb(var(--link-6))',
      count: 1000000,
    },
    {
      label: t('workbench.homePage.bugCount'),
      value: WorkOverviewEnum.BUG_COUNT,
      icon: WorkOverviewIconEnum.BUG_COUNT,
      color: 'rgb(var(--danger-6))',
      count: 1000000,
    },
  ]);

  const hasRoom = computed(() => projectIds.value.length >= 7);
  // 静态数据 TODO
  const members = computed(() => ['张三', '李四', '王五', '小王']);

  const staticData = [
    {
      name: '',
      type: 'bar',
      stack: 'member',
      barWidth: 12,
      data: [400, 200, 150, 80, 70, 110, 130],
    },
    {
      name: '',
      type: 'bar',
      stack: 'member',
      barWidth: 12,
      data: [90, 160, 130, 100, 90, 120, 140],
    },
    {
      name: '',
      type: 'bar',
      stack: 'member',
      barWidth: 12,
      data: [100, 140, 120, 90, 100, 130, 120],
    },
    {
      name: '',
      type: 'bar',
      stack: 'member',
      barWidth: 12,
      data: [90, 160, 130, 100, 90, 120, 140],
    },
    {
      name: '',
      type: 'bar',
      barWidth: 12,
      stack: 'member',
      data: [100, 140, 120, 90, 100, 130, 120],
    },
    {
      name: '',
      type: 'bar',
      barWidth: 12,
      stack: 'member',
      data: [100, 140, 120, 90, 40, 130, 120],
    },
    {
      name: '',
      type: 'bar',
      stack: 'member',
      barWidth: 12,
      data: [100, 140, 120, 90, 40, 130, 120],
      itemStyle: {
        borderRadius: [2, 2, 0, 0], // 上边圆角
      },
    },
  ];

  const lastSeriousData = computed(() => {
    contentTabList.value.forEach((e, i) => {
      staticData[i].name = e.label;
    });
    return staticData;
  });

  const options = ref({
    tooltip: {
      trigger: 'axis',
      borderWidth: 0,
      formatter(params: any) {
        const html = `
      <div class="w-[186px] h-[auto] p-[16px] flex flex-col">
      ${params
        .map(
          (item: any) => `
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center">
            <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm" style="background:${item.color}"></div>
            <div style="color:#959598">${item.seriesName}</div>
          </div>
          <div class="text-[#323233] font-medium">${addCommasToNumber(item.value)}</div>
        </div>
      `
        )
        .join('')}
      </div>
    `;
        return html;
      },
    },
    color: commonColorConfig,
    grid: {
      top: '36px',
      left: '10px',
      right: '10px',
      bottom: hasRoom.value ? '54px' : '5px',
      containLabel: true,
    },
    xAxis: {
      splitLine: false,
      boundaryGap: true,
      type: 'category',
      data: members.value,
      axisLabel: {
        color: '#646466',
      },
      axisTick: {
        show: false, // 隐藏刻度线
      },
      axisLine: {
        lineStyle: {
          color: '#EDEDF1',
        },
      },
    },
    yAxis: [
      {
        type: 'value',
        name: '单位：个', // 设置单位
        nameLocation: 'end',
        nameTextStyle: {
          fontSize: 12,
          color: '#AEAEB2', // 自定义字体大小和颜色
          padding: [0, 0, 0, -20], // 通过左侧（最后一个值）的负偏移向左移动
        },
        nameGap: 20,
        splitLine: {
          show: true, // 控制是否显示水平线
          lineStyle: {
            color: '#EDEDF1', // 水平线颜色
            width: 1, // 水平线宽度
            type: 'dashed', // 水平线线型，可选 'solid'、'dashed'、'dotted'
          },
        },
      },
    ],
    colorBy: 'series',
    series: lastSeriousData,
    legend: {
      show: true,
      type: 'scroll',
      itemGap: 20,
      itemWidth: 8,
      itemHeight: 8,
    },
    dataZoom: hasRoom.value
      ? [
          {
            type: 'inside',
          },
          {
            type: 'slider',
          },
        ]
      : [],
  });
</script>

<style scoped lang="less"></style>
