<template>
  <div class="card-wrapper api-count-wrapper card-min-height">
    <CardSkeleton v-if="showSkeleton" :show-skeleton="showSkeleton" />
    <div v-else>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div>
          <MsSelect
            v-model:model-value="projectId"
            :options="appStore.projectList"
            allow-search
            value-key="id"
            label-key="name"
            :search-keys="['name']"
            class="!w-[200px]"
            :prefix="t('workbench.homePage.project')"
            @change="changeProject"
          >
          </MsSelect>
        </div>
      </div>
      <div class="my-[16px]">
        <div class="flex gap-[16px]">
          <div
            v-for="tabItem of apiCountTabList"
            :key="tabItem.label"
            :class="`flex-1  ${tabItem.value === 'complete' ? 'min-w-[300px]' : 'min-w-[197px]'}`"
          >
            <PassRatePie
              :tooltip-text="tabItem.tooltip"
              :project-id="projectId"
              :options="tabItem.options"
              :loading="tabItem.value === 'cover' ? loading : undefined"
              :has-permission="hasPermission"
              :value-list="tabItem.valueList"
            />
          </div>
        </div>
        <div class="mt-[16px] h-[148px]">
          <LegendPieChart
            v-model:currentPage="currentPage"
            :has-permission="hasPermission"
            :data="statusPercentValue"
            :options="apiCountOptions"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 接口数量
   */
  import { ref } from 'vue';

  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import LegendPieChart, { legendDataType } from './legendPieChart.vue';
  import PassRatePie from './passRatePie.vue';

  import { workApiCountCoverRage, workApiCountDetail } from '@/api/modules/workbench';
  import getVisualThemeColor from '@/config/chartTheme';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { ApiCoverageData, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { colorMapConfig, handlePieData, handleUpdateTabPie } from '../utils';

  const { t } = useI18n();
  const appStore = useAppStore();

  const props = defineProps<{
    item: SelectedCardItem;
    refreshKey: number;
    projectIds: string[];
    status?: boolean;
    cover?: ApiCoverageData;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const loading = ref<boolean>(false);

  const projectId = ref<string>(innerProjectIds.value[0]);
  const currentPage = ref(1);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const coverValueList = ref<{ value: string | number; label: string; name: string }[]>([]);

  const completeValueList = ref<{ value: string | number; label: string; name: string }[]>([]);
  const coverOptions = ref<Record<string, any>>({});
  const completeOptions = ref<Record<string, any>>({});
  const apiCountTabList = computed(() => {
    return [
      {
        label: '',
        value: 'cover',
        valueList: coverValueList.value,
        options: { ...coverOptions.value },
        tooltip: 'workbench.homePage.apiCountCoverRateTooltip',
      },
      {
        label: '',
        value: 'complete',
        valueList: completeValueList.value,
        options: { ...completeOptions.value },
        tooltip: 'workbench.homePage.apiCountCompleteRateTooltip',
      },
    ];
  });

  const apiCountOptions = ref({});
  const hasPermission = ref<boolean>(false);

  const statusPercentValue = ref<legendDataType[]>([]);

  async function handleCoverData(detail: ApiCoverageData) {
    const { unCoverWithApiDefinition, coverWithApiDefinition, apiCoverage } = detail;
    const coverData: {
      name: string;
      count: number;
    }[] = [
      {
        count: Number(apiCoverage.split('%')[0]),
        name: t('workbench.homePage.coverRate'),
      },
      {
        count: coverWithApiDefinition,
        name: t('workbench.homePage.covered'),
      },
      {
        count: unCoverWithApiDefinition,
        name: t('workbench.homePage.notCover'),
      },
    ];
    const { options: covOptions, valueList: coverList } = handleUpdateTabPie(
      coverData,
      hasPermission.value,
      `${props.item.key}-cover`
    );
    coverValueList.value = [...coverList];
    coverOptions.value = { ...covOptions };
  }

  async function initApiCountRate() {
    try {
      loading.value = true;
      const detail = await workApiCountCoverRage(projectId.value);
      handleCoverData(detail);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const showSkeleton = ref(false);

  async function initApiCount() {
    try {
      showSkeleton.value = true;

      const { startTime, endTime, dayNumber } = timeForm.value;
      const detail = await workApiCountDetail({
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: [],
      });
      const { statusStatisticsMap, statusPercentList, errorCode } = detail;
      statusPercentValue.value = (statusPercentList || []).map((item, index) => {
        return {
          ...item,
          selected: true,
          color: `${
            colorMapConfig[props.item.key][index] !== 'initItemStyleColor'
              ? colorMapConfig[props.item.key][index]
              : getVisualThemeColor('initItemStyleColor')
          }`,
        };
      });

      hasPermission.value = errorCode !== 109001;
      apiCountOptions.value = handlePieData(props.item.key, hasPermission.value, statusPercentList);

      // 完成率
      const { options: comOptions, valueList: completedList } = handleUpdateTabPie(
        statusStatisticsMap?.completionRate || [],
        hasPermission.value,
        `${props.item.key}-complete`
      );
      completeValueList.value = completedList;
      completeOptions.value = comOptions;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }

  function changeProject() {
    nextTick(async () => {
      emit('change');
    });
  }
  const isInit = ref(true);

  onMounted(() => {
    isInit.value = false;
    initApiCount();
    emit('change');
  });

  watch(
    () => props.cover,
    (val) => {
      if (val) {
        handleCoverData(val);
      }
    },
    {
      deep: true,
      immediate: true,
    }
  );

  watch(
    () => hasPermission.value,
    () => {
      if (props.cover) {
        handleCoverData(props.cover);
      }
    }
  );

  watch(
    () => props.status,
    (val) => {
      loading.value = val;
    }
  );

  watch(
    () => props.item.projectIds,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
      }
    },
    { immediate: true }
  );

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
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

  watch([() => props.refreshKey, () => projectId.value], async () => {
    await nextTick();
    await initApiCount();
    if (hasPermission.value && !isInit.value) {
      initApiCountRate();
    }
  });
</script>

<style scoped lang="less">
  .card-wrapper.api-count-wrapper {
    padding-bottom: 4px;
  }
</style>
