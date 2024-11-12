<template>
  <div class="card-wrapper api-count-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.apiCount') }} </div>
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
    <div class="my-[16px]">
      <TabCard :content-tab-list="apiCountTabList" not-has-padding hidden-border min-width="270px">
        <template #item="{ item: tabItem }">
          <div class="w-full">
            <PassRatePie
              :tooltip-text="tabItem.tooltip"
              :options="tabItem.options"
              :size="60"
              :value-list="tabItem.valueList"
            />
          </div>
        </template>
      </TabCard>
      <div class="h-[148px]">
        <MsChart :options="apiCountOptions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 接口数量
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
    projectIds: string[];
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

  const options = ref(cloneDeep(commonRatePieOptions));

  // TODO 假数据
  const detail = ref<PassRateDataType>({
    statusStatisticsMap: {
      cover: [
        { name: '覆盖率', count: 10 },
        { name: '已覆盖', count: 2 },
        { name: '未覆盖', count: 1 },
      ],
      success: [
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

  const coverValueList = ref([
    {
      label: t('workbench.homePage.covered'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.notCover'),
      value: 2000,
    },
  ]);
  const passValueList = ref([
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
  const coverOptions = ref<Record<string, any>>(cloneDeep(options.value));
  const completeOptions = ref<Record<string, any>>(cloneDeep(options.value));
  const apiCountTabList = computed(() => {
    return [
      {
        label: '',
        value: 'execution',
        valueList: coverValueList.value,
        options: { ...coverOptions.value },
        tooltip: 'workbench.homePage.apiCountCoverRateTooltip',
      },
      {
        label: '',
        value: 'pass',
        valueList: passValueList.value,
        options: { ...completeOptions.value },
        tooltip: 'workbench.homePage.apiCountCompleteRateTooltip',
      },
    ];
  });

  const apiCountOptions = ref({});

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
    const { cover, success } = statusStatisticsMap;
    coverValueList.value = handlePassRatePercent(cover);
    passValueList.value = handlePassRatePercent(success);

    coverOptions.value.series.data = handlePassRatePercent(cover);
    completeOptions.value.series.data = handlePassRatePercent(success);

    coverOptions.value.title.text = cover[0].name ?? '';
    coverOptions.value.title.subtext = `${cover[0].count ?? 0}%`;

    completeOptions.value.title.text = success[0].name ?? '';
    completeOptions.value.title.subtext = `${success[0].count ?? 0}%`;

    coverOptions.value.series.color = ['#00C261', '#D4D4D8'];
    completeOptions.value.series.color = ['#00C261', '#ED0303'];
  }

  function initApiCount() {
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
      apiCountOptions.value = handlePieData(props.item.key, statusPercentList);
      handleRatePieData(statusStatisticsMap);
    } catch (error) {
      console.log(error);
    }
  }

  onMounted(() => {
    initApiCount();
  });

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
        initApiCount();
      }
    }
  );

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
        initApiCount();
      }
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initApiCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less">
  .card-wrapper.api-count-wrapper {
    padding-bottom: 4px;
  }
</style>
