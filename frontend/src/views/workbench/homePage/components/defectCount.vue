<template>
  <div class="card-wrapper card-min-height">
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
      <div class="mt-[16px]">
        <div class="case-count-wrapper">
          <div class="case-count-item mb-[16px]">
            <PassRatePie
              :has-permission="hasPermission"
              :tooltip-text="tooltip"
              :options="legacyOptions"
              :project-id="projectId"
              :value-list="legacyValueList"
            />
          </div>
        </div>
        <div class="flex h-[148px]">
          <LegendPieChart
            v-model:currentPage="currentPage"
            :has-permission="hasPermission"
            :data="statusPercentValue"
            :options="countOptions"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 用于缺陷数量，待我处理的缺陷数量组件
   */
  import { ref } from 'vue';

  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import LegendPieChart, { legendDataType } from './legendPieChart.vue';
  import PassRatePie from './passRatePie.vue';

  import {
    workBugByMeCreated,
    workBugCountDetail,
    workBugHandleByMe,
    workPlanLegacyBug,
  } from '@/api/modules/workbench';
  import getVisualThemeColor from '@/config/chartTheme';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type {
    PassRateDataType,
    SelectedCardItem,
    TimeFormParams,
    WorkHomePageDetail,
  } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  import { colorMapConfig, handlePieData, handleUpdateTabPie, routeNavigationMap } from '../utils';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
    refreshKey: number;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const legacyValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const legacyOptions = ref<Record<string, any>>({});

  const countOptions = ref<Record<string, any>>({});

  type SelectedBugCountKeys =
    | WorkCardEnum.BUG_COUNT
    | WorkCardEnum.HANDLE_BUG_BY_ME
    | WorkCardEnum.CREATE_BUG_BY_ME
    | WorkCardEnum.PLAN_LEGACY_BUG;

  const currentBugCount: (data: WorkHomePageDetail) => Promise<PassRateDataType> = {
    [WorkCardEnum.BUG_COUNT]: workBugCountDetail,
    [WorkCardEnum.HANDLE_BUG_BY_ME]: workBugHandleByMe,
    [WorkCardEnum.CREATE_BUG_BY_ME]: workBugByMeCreated,
    [WorkCardEnum.PLAN_LEGACY_BUG]: workPlanLegacyBug,
  }[props.item.key as SelectedBugCountKeys];

  const hasPermission = ref<boolean>(false);
  const showSkeleton = ref(false);
  const statusPercentValue = ref<legendDataType[]>([]);

  async function initCount() {
    try {
      showSkeleton.value = true;
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

      const detail = await currentBugCount(params);

      const { statusStatisticsMap, statusPercentList, errorCode } = detail;
      hasPermission.value = errorCode !== 109001;
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

      countOptions.value = handlePieData(props.item.key, hasPermission.value, statusPercentList);
      if (props.item.key === WorkCardEnum.PLAN_LEGACY_BUG) {
        countOptions.value.title.text = t('workbench.homePage.legacyDefectsNumber');
      }
      const [rate, totalBug, legacy] = statusStatisticsMap?.retentionRate || [];

      const unLegacy = totalBug && legacy ? totalBug.count - legacy.count : 0;

      const unLegacyItem = { name: t('workbench.homePage.notLegacyDefectsNumber'), count: unLegacy };

      const legacyData = [rate, legacy, unLegacyItem];

      const { options, valueList } = handleUpdateTabPie(legacyData, hasPermission.value, `${props.item.key}-legacy`);

      legacyValueList.value = hasPermission.value
        ? (statusStatisticsMap?.retentionRate || []).slice(1).map((item, i) => {
            return {
              value: item.count,
              label: item.name,
              name: item.name,
              route: routeNavigationMap[props.item.key].legacy?.route,
              status: routeNavigationMap[props.item.key].legacy?.status[i],
            };
          })
        : valueList;

      legacyOptions.value = options;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }

  const tooltip = computed(() => {
    return props.item.key === WorkCardEnum.PLAN_LEGACY_BUG ? 'workbench.homePage.planCaseCountLegacyRateTooltip' : '';
  });

  function changeProject() {
    nextTick(() => {
      emit('change');
    });
  }

  const currentPage = ref(1);

  onMounted(() => {
    initCount();
  });

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
      innerProjectIds.value = [val];
    },
    {
      immediate: true,
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

  watch([() => props.refreshKey, () => projectId.value], async () => {
    await nextTick();
    initCount();
  });
</script>

<style scoped lang="less"></style>
