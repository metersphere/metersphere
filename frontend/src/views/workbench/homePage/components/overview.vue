<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ props.title }} </div>
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
   * @desc 概览
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import TabCard from './tabCard.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { WorkOverviewEnum, WorkOverviewIconEnum } from '@/enums/workbenchEnum';

  import { commonColorConfig, getCommonBarOptions } from '../utils';

  const { t } = useI18n();

  const props = defineProps<{
    title: string;
  }>();
  const appStore = useAppStore();

  const projectIds = ref('');

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
      value: WorkOverviewEnum.TEST_PLAN,
      icon: WorkOverviewIconEnum.TEST_PLAN,
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

  const xAxisType = computed(() => {
    return contentTabList.value.map((e) => e.label);
  });

  const hasRoom = computed(() => projectIds.value.length >= 7);

  const options = ref<Record<string, any>>({});
  const seriesData = ref<Record<string, any>[]>([
    {
      name: '项目A',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0], // 上边圆角
      },
      data: [null, 230, 150, 80, 70, 110, 130],
    },
    {
      name: '项目B',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0], // 上边圆角
      },
      data: [90, 160, 130, 100, 90, 120, 140],
    },
    {
      name: '项目C',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0], // 上边圆角
      },
      data: [100, 140, 120, 90, 100, 130, 120],
    },
    {
      name: '项目D',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0], // 上边圆角
      },
      data: [90, 160, 130, 100, 90, 120, 140],
    },
    {
      name: '项目E',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0], // 上边圆角
      },
      data: [100, 140, 120, 90, 100, 130, 120],
    },
    {
      name: '项目F',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0], // 上边圆角
      },
      data: [100, 140, 120, 90, 40, 130, 120],
    },
    {
      name: '项目G',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0], // 上边圆角
      },
      data: [100, 140, 120, 90, 40, 130, 120],
    },
  ]);

  onMounted(() => {
    options.value = getCommonBarOptions(hasRoom.value, commonColorConfig);
    options.value.xAxis.data = xAxisType.value;
    options.value.series = seriesData.value;
  });
</script>

<style scoped lang="less">
  :deep(.arco-select-view-multiple.arco-select-view-size-medium .arco-select-view-tag) {
    margin-top: 1px;
    margin-bottom: 1px;
    max-width: 80px;
    height: auto;
    min-height: 24px;
    line-height: 22px;
    vertical-align: middle;
  }
</style>
