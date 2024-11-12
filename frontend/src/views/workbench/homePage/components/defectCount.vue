<template>
  <div class="card-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t(props.item.label) }} </div>
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
      <div class="case-count-wrapper">
        <div class="case-count-item mb-[16px]">
          <PassRatePie :tooltip-text="tooltip" :options="legacyOptions" :size="60" :value-list="valueList" />
        </div>
      </div>
      <div class="h-[148px]">
        <MsChart :options="countOptions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 用于缺陷数量，待我处理的缺陷数量组件
   */
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type {
    PassRateDataType,
    SelectedCardItem,
    StatusStatisticsMapType,
    TimeFormParams,
  } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  import { commonRatePieOptions, handlePieData } from '../utils';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
  }>();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const valueList = ref<
    {
      label: string;
      value: number;
    }[]
  >([
    {
      label: t('workbench.homePage.defectTotal'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.legacyDefectsNumber'),
      value: 2000,
    },
  ]);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const legacyOptions = ref<Record<string, any>>(cloneDeep(commonRatePieOptions));

  // TODO 假数据
  const detail = ref<PassRateDataType>({
    statusStatisticsMap: {
      legacy: [
        { name: '遗留率', count: 10 },
        { name: '缺陷总数', count: 2 },
        { name: '遗留缺陷数', count: 1 },
      ],
    },
    statusPercentList: [
      { status: 'AAA', count: 1, percentValue: '10%' },
      { status: 'BBB', count: 3, percentValue: '0%' },
      { status: 'CCC', count: 6, percentValue: '0%' },
    ],
  });

  const countOptions = ref({});

  function handleRatePieData(statusStatisticsMap: StatusStatisticsMapType) {
    const { legacy } = statusStatisticsMap;
    valueList.value = legacy.slice(1).map((item) => {
      return {
        value: item.count,
        label: item.name,
        name: item.name,
      };
    });
    legacyOptions.value.series.data = valueList.value;

    legacyOptions.value.title.text = legacy[0].name ?? '';
    legacyOptions.value.title.subtext = `${legacy[0].count ?? 0}%`;
    legacyOptions.value.series.color = ['#D4D4D8', '#00C261'];
  }

  async function initCount() {
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
      countOptions.value = handlePieData(props.item.key, statusPercentList);
      handleRatePieData(statusStatisticsMap);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const tooltip = computed(() => {
    return props.item.key === WorkCardEnum.PLAN_LEGACY_BUG ? 'workbench.homePage.planCaseCountLegacyRateTooltip' : '';
  });

  onMounted(() => {
    initCount();
  });

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
        initCount();
      }
    }
  );

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
        initCount();
      }
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less"></style>
