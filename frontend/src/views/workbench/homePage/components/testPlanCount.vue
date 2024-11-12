<template>
  <div class="card-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title">
        {{ t('workbench.homePage.numberOfTestPlan') }}
      </div>
      <div>
        <MsSelect
          v-model:model-value="projectId"
          :options="appStore.projectList"
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
      <TabCard :content-tab-list="testPlanTabList" not-has-padding hidden-border min-width="270px">
        <template #item="{ item: tabItem }">
          <div class="w-full">
            <PassRatePie :options="tabItem.options" :size="60" :value-list="tabItem.valueList" />
          </div>
        </template>
      </TabCard>
      <div class="h-[148px]">
        <MsChart :options="testPlanCountOptions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 测试计划数量
   */
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';
  import TabCard from './tabCard.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type {
    PassRateDataType,
    SelectedCardItem,
    StatusStatisticsMapType,
    TimeFormParams,
  } from '@/models/workbench/homePage';

  import { commonRatePieOptions, handlePieData } from '../utils';

  const props = defineProps<{
    item: SelectedCardItem;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  // TODO 假数据后边几个要用
  const detail = ref<PassRateDataType>({
    statusStatisticsMap: {
      execute: [
        { name: '覆盖率', count: 10 },
        { name: '已覆盖', count: 2 },
        { name: '未覆盖', count: 1 },
      ],
      pass: [
        { name: '覆盖率', count: 10 },
        { name: '已覆盖', count: 2 },
        { name: '未覆盖', count: 1 },
      ],
      complete: [
        { name: '覆盖率', count: 10 },
        { name: '已覆盖', count: 2 },
        { name: '未覆盖', count: 1 },
      ],
    },
    statusPercentList: [
      { status: 'HTTP', count: 1, percentValue: '10%' },
      { status: 'TCP', count: 3, percentValue: '0%' },
      { status: 'BBB', count: 6, percentValue: '0%' },
    ],
  });

  const options = ref(cloneDeep(commonRatePieOptions));
  const executionOptions = ref<Record<string, any>>(cloneDeep(options.value));
  const passOptions = ref<Record<string, any>>(cloneDeep(options.value));
  const completeOptions = ref<Record<string, any>>(cloneDeep(options.value));

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
        options: { ...executionOptions.value },
      },
      {
        label: '',
        value: 'pass',
        valueList: passValueList.value,
        options: { ...passOptions.value },
      },
      {
        label: '',
        value: 'complete',
        valueList: completeValueList.value,
        options: { ...completeOptions.value },
      },
    ];
  });

  function handlePassRatePercent(data: { name: string; count: number }[]) {
    return data.slice(1).map((item) => {
      return {
        value: item.count,
        label: item.name,
        name: item.name,
      };
    });
  }

  function handleRatePieData(statusStatisticsMap: StatusStatisticsMapType) {
    const { execute, pass, complete } = statusStatisticsMap;
    executionValueList.value = handlePassRatePercent(execute);
    passValueList.value = handlePassRatePercent(pass);
    completeValueList.value = handlePassRatePercent(complete);

    executionOptions.value.series.data = handlePassRatePercent(execute);
    passOptions.value.series.data = handlePassRatePercent(pass);
    completeOptions.value.series.data = handlePassRatePercent(complete);

    executionOptions.value.title.text = execute[0].name ?? '';
    executionOptions.value.title.subtext = `${execute[0].count ?? 0}%`;

    passOptions.value.title.text = pass[0].name ?? '';
    passOptions.value.title.subtext = `${pass[0].count ?? 0}%`;

    completeOptions.value.title.text = complete[0].name ?? '';
    completeOptions.value.title.subtext = `${complete[0].count ?? 0}%`;

    executionOptions.value.series.color = ['#D4D4D8', '#00C261'];
    passOptions.value.series.color = ['#D4D4D8', '#00C261'];
    completeOptions.value.series.color = ['#00C261', '#3370FF', '#D4D4D8'];
  }

  const testPlanCountOptions = ref({});
  async function initTestPlanCount() {
    try {
      const { startTime, endTime, dayNumber } = timeForm.value;
      const params = {
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: [],
      };
      const { statusStatisticsMap, statusPercentList } = detail.value;

      testPlanCountOptions.value = handlePieData(props.item.key, statusPercentList);
      handleRatePieData(statusStatisticsMap);
    } catch (error) {
      console.log(error);
    }
  }

  onMounted(() => {
    initTestPlanCount();
  });

  onMounted(() => {
    initTestPlanCount();
  });

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
        initTestPlanCount();
      }
    }
  );

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
        initTestPlanCount();
      }
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initTestPlanCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less">
  :deep(.arco-tabs-tab) {
    padding: 0 !important;
  }
</style>
