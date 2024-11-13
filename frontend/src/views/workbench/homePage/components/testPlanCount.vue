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
          @change="changeProject"
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <TabCard :content-tab-list="testPlanTabList" not-has-padding hidden-border min-width="270px">
        <template #item="{ item: tabItem }">
          <div class="w-full">
            <PassRatePie
              :has-permission="hasPermission"
              :options="tabItem.options"
              :size="60"
              :value-list="tabItem.valueList"
            />
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

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';
  import TabCard from './tabCard.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { PassRateDataType, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { handlePieData, handleUpdateTabPie } from '../utils';

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
    errorCode: 109001,
  });

  const executionOptions = ref<Record<string, any>>({});
  const passOptions = ref<Record<string, any>>({});
  const completeOptions = ref<Record<string, any>>({});

  // 执行率
  const executionValueList = ref<{ value: number | string; label: string; name: string }[]>([]);
  // 通过率
  const passValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  // 完成率
  const completeValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

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

  const testPlanCountOptions = ref({});
  const hasPermission = ref<boolean>(false);
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
      const { statusStatisticsMap, statusPercentList, errorCode } = detail.value;

      hasPermission.value = errorCode !== 109001;
      testPlanCountOptions.value = handlePieData(props.item.key, hasPermission.value, statusPercentList);

      // 执行率
      const { options: executedOptions, valueList: executedList } = handleUpdateTabPie(
        statusStatisticsMap?.execute || [],
        hasPermission.value,
        `${props.item.key}-execute`
      );

      // 通过率
      const { options: passedOptions, valueList: passList } = handleUpdateTabPie(
        statusStatisticsMap?.pass || [],
        hasPermission.value,
        `${props.item.key}-pass`
      );

      // 完成率
      const { options: comOptions, valueList: completeList } = handleUpdateTabPie(
        statusStatisticsMap?.complete || [],
        hasPermission.value,
        `${props.item.key}-complete`
      );

      executionValueList.value = executedList;
      passValueList.value = passList;
      completeValueList.value = completeList;

      executionOptions.value = executedOptions;
      passOptions.value = passedOptions;
      completeOptions.value = comOptions;
    } catch (error) {
      console.log(error);
    }
  }

  function changeProject() {
    initTestPlanCount();
  }

  onMounted(() => {
    initTestPlanCount();
  });

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
      }
    }
  );

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
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
