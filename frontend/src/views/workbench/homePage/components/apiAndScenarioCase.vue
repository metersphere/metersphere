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
          <div class="case-count-item">
            <div v-for="(ele, index) of executionTimeValue" :key="index" class="case-count-item-content">
              <div class="case-count-item-title">{{ ele.name }}</div>
              <div class="case-count-item-number">
                {{ hasPermission ? addCommasToNumber(ele.count as number) : '-' }}
              </div>
            </div>
          </div>
          <div class="case-count-item">
            <div v-for="(ele, index) of apiCountValue" :key="index" class="case-count-item-content">
              <div class="case-count-item-title">{{ ele.name }}</div>
              <div
                :class="`case-count-item-number ${index !== 0 ? 'cursor-pointer text-[rgb(var(--primary-5))]' : ''}`"
                @click="goNavigation(index)"
              >
                {{ hasPermission ? addCommasToNumber(ele.count as number) : '-' }}
              </div>
            </div>
          </div>
        </div>
        <div class="case-ratio-wrapper mt-[16px]">
          <div class="case-ratio-item">
            <RatioPie
              :value-key="props.item.key"
              :has-permission="hasPermission"
              :loading="loading"
              :data="coverData"
              :project-id="projectId"
              :rate-config="coverTitleConfig"
            />
          </div>
          <div class="case-ratio-item">
            <RatioPie
              :value-key="props.item.key"
              :project-id="projectId"
              :has-permission="hasPermission"
              :data="caseExecuteData"
              :rate-config="executeTitleConfig"
            />
          </div>
          <div class="case-ratio-item">
            <RatioPie
              :value-key="props.item.key"
              :project-id="projectId"
              :has-permission="hasPermission"
              :data="casePassData"
              :rate-config="casePassTitleConfig"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 接口用例数量/场景用例数量
   */
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import RatioPie from './ratioPie.vue';

  import { workApiCaseCountDetail, workApiCountCoverRage, workScenarioCaseCountDetail } from '@/api/modules/workbench';
  import getVisualThemeColor from '@/config/chartTheme';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import { addCommasToNumber } from '@/utils';

  import type { ApiCoverageData, SelectedCardItem, StatusValueItem, TimeFormParams } from '@/models/workbench/homePage';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { WorkCardEnum, WorkNavValueEnum } from '@/enums/workbenchEnum';

  import { routeNavigationMap } from '../utils';

  const { openNewPage } = useOpenNewPage();

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
    refreshKey: number;
    status?: boolean;
    cover?: ApiCoverageData;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const executionTimeValue = ref<{ name: string; count: number | string }[]>([
    {
      name: t('workbench.homePage.executionTimes'),
      count: '-',
    },
  ]);

  const apiCountValue = ref<{ name: string; count: number | string }[]>([
    {
      name:
        props.item.key === WorkCardEnum.API_CASE_COUNT
          ? t('workbench.homePage.apiUseCasesNumber')
          : t('workbench.homePage.scenarioUseCasesNumber'),
      count: '-',
    },
    {
      name: t('workbench.homePage.misstatementCount'),
      count: '-',
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

  const initCoverRate: StatusValueItem[] = [
    {
      value: 0,
      name: t('workbench.homePage.coverRate'),
    },
    {
      value: 0,
      name: t('workbench.homePage.notCover'),
    },
    {
      value: 0,
      name: t('workbench.homePage.covered'),
    },
  ];

  // 接口覆盖
  const coverData = ref<StatusValueItem[]>(cloneDeep(initCoverRate));
  const caseExecuteData = ref<StatusValueItem[]>([
    {
      value: 0,
      name: t('common.unExecute'),
    },
    {
      value: 0,
      name: t('common.executed'),
    },
  ]);

  const casePassData = ref<StatusValueItem[]>([
    {
      value: 0,
      name: t('workbench.homePage.notPass'),
    },
    {
      value: 0,
      name: t('workbench.homePage.havePassed'),
    },
  ]);

  const coverTitleConfig = computed(() => {
    return {
      name: t('workbench.homePage.apiCoverage'),
      color: [getVisualThemeColor('initItemStyleColor'), '#00C261'],
      tooltipText:
        props.item.key === WorkCardEnum.API_CASE_COUNT
          ? t('workbench.homePage.apiCaseCountCoverRateTooltip')
          : t('workbench.homePage.scenarioCaseCountCoverRateTooltip'),
    };
  });

  const executeTitleConfig = computed(() => {
    return props.item.key === WorkCardEnum.API_CASE_COUNT
      ? {
          name: t('workbench.homePage.caseExecutionRate'),
          color: [getVisualThemeColor('initItemStyleColor'), '#00C261'],
          tooltipText: t('workbench.homePage.apiCaseCountExecuteRateTooltip'),
        }
      : {
          name: t('workbench.homePage.sceneExecutionRate'),
          color: [getVisualThemeColor('initItemStyleColor'), '#00C261'],
          tooltipText: t('workbench.homePage.scenarioCaseCountExecuteRateTooltip'),
        };
  });

  const casePassTitleConfig = computed(() => {
    return props.item.key === WorkCardEnum.API_CASE_COUNT
      ? {
          name: t('workbench.homePage.casePassedRate'),
          color: ['#ED0303', '#00C261'],
          tooltipText: t('workbench.homePage.apiCaseCountPassRateTooltip'),
        }
      : {
          name: t('workbench.homePage.executionRate'),
          color: ['#ED0303', '#00C261'],
          tooltipText: t('workbench.homePage.scenarioCaseCountPassRateTooltip'),
        };
  });

  const hasPermission = ref<boolean>(false);
  const loading = ref<boolean>(false);

  function handleCoverData(detail: ApiCoverageData) {
    const { unCoverWithApiCase, coverWithApiCase, apiCaseCoverage } = detail;
    const { unCoverWithApiScenario, coverWithApiScenario, scenarioCoverage } = detail;

    const coverage = props.item.key === WorkCardEnum.API_CASE_COUNT ? apiCaseCoverage : scenarioCoverage;
    const unCoverWithCase =
      props.item.key === WorkCardEnum.API_CASE_COUNT ? unCoverWithApiCase : unCoverWithApiScenario;

    const coverWithCase = props.item.key === WorkCardEnum.API_CASE_COUNT ? coverWithApiCase : coverWithApiScenario;

    coverData.value = cloneDeep(initCoverRate);

    const coverList: StatusValueItem[] = [
      {
        value: Number(coverage.split('%')[0]),
        name: t('workbench.homePage.coverRate'),
      },
      {
        value: unCoverWithCase,
        name: t('workbench.homePage.notCover'),
      },
      {
        value: coverWithCase,
        name: t('workbench.homePage.covered'),
      },
    ];
    coverData.value = coverList.map((e, i) => {
      if (i === 0) {
        return {
          ...e,
        };
      }
      return {
        ...e,
        route: routeNavigationMap[props.item.key].cover?.route,
        status: routeNavigationMap[props.item.key].cover?.status[i - 1],
      };
    });
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

  const generateCaseData = (dataList: { name: string; count: number }[], rateData: Record<string, any>) => {
    return (dataList || []).map((e, i) => {
      const baseItem = {
        ...e,
        value: e.count,
      };

      if (i === 0) {
        return baseItem;
      }

      return {
        ...baseItem,
        route: rateData?.route,
        status: rateData?.status?.[i - 1],
        ...(props.item.key === WorkCardEnum.API_CASE_COUNT && { tab: 'case' }),
      };
    });
  };

  const showSkeleton = ref(false);

  async function initApiOrScenarioCount() {
    try {
      showSkeleton.value = true;
      coverData.value = cloneDeep(initCoverRate);
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
      let detail;
      if (props.item.key === WorkCardEnum.API_CASE_COUNT) {
        detail = await workApiCaseCountDetail(params);
      } else {
        detail = await workScenarioCaseCountDetail(params);
      }

      hasPermission.value = detail.errorCode !== 109001;

      const execRateData = detail.statusStatisticsMap?.execRate || [];
      const executeRateConfig = routeNavigationMap[props.item.key]?.executeRate;
      const passRateData = detail.statusStatisticsMap?.passRate || [];
      const passRateConfig = routeNavigationMap[props.item.key]?.passRate;

      caseExecuteData.value = generateCaseData(execRateData, executeRateConfig);
      casePassData.value = generateCaseData(passRateData, passRateConfig);

      if (hasPermission.value) {
        // 执行次数
        executionTimeValue.value = detail.statusStatisticsMap?.execCount || [];
        // 数量
        const valueKey = props.item.key === WorkCardEnum.API_CASE_COUNT ? 'apiCaseCount' : 'apiScenarioCount';
        apiCountValue.value = detail.statusStatisticsMap?.[valueKey] || [];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }

  function changeProject() {
    nextTick(() => {
      emit('change');
    });
  }

  function goNavigation(index: number) {
    if (index === 0) {
      return;
    }
    const route =
      props.item.key === WorkCardEnum.API_CASE_COUNT
        ? ApiTestRouteEnum.API_TEST_MANAGEMENT
        : ApiTestRouteEnum.API_TEST_SCENARIO;
    const status =
      props.item.key === WorkCardEnum.API_CASE_COUNT
        ? WorkNavValueEnum.API_COUNT_EXECUTE_FAKE_ERROR
        : WorkNavValueEnum.SCENARIO_COUNT_EXECUTE_FAKE_ERROR;

    const params: { pId: string; home: string; tab?: string } = {
      pId: projectId.value,
      home: status,
    };
    if (props.item.key === WorkCardEnum.API_CASE_COUNT) {
      params.tab = 'case';
    }
    openNewPage(route, params);
  }

  const isInit = ref<boolean>(true);

  onMounted(() => {
    isInit.value = false;
    initApiOrScenarioCount();
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
        initApiOrScenarioCount();
      }
    },
    {
      deep: true,
    }
  );

  watch([() => props.refreshKey, () => projectId.value], async () => {
    await nextTick();
    await initApiOrScenarioCount();
    if (hasPermission.value && !isInit.value) {
      initApiCountRate();
    }
  });
</script>

<style scoped lang="less">
  .case-count-wrapper {
    @apply flex items-center gap-4;
    .case-count-item {
      padding: 16px;
      border-radius: 6px;
      background: var(--color-text-n9);
      @apply flex items-center;
      .case-count-item-content {
        @apply flex-1;
        .case-count-item-count {
          @apply flex-1;
        }
        .case-count-item-title {
          margin-bottom: 8px;
          color: var(--color-text-4);
        }
        .case-count-item-number {
          font-size: 20px;
          @apply font-medium;
        }
      }
    }
  }
  .case-ratio-wrapper {
    @apply flex;
    .case-ratio-item {
      @apply flex flex-1 items-center justify-center;
    }
  }
</style>
