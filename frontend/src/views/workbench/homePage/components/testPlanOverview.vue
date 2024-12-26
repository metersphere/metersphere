<template>
  <div class="card-wrapper">
    <CardSkeleton v-if="showSkeleton" :show-skeleton="showSkeleton" />
    <div v-else>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl" :mouse-enter-delay="300">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div class="cascader-wrapper-self relative flex justify-end">
          <MsCascader
            v-model:model-value="selectValue"
            mode="native"
            allow-search
            :allow-clear="false"
            :default-value="defaultValue"
            :options="projectOptions"
            :prefix="t('workbench.homePage.plan')"
            :placeholder="t('workbench.homePage.planOfPleaseSelect')"
            :virtual-list-props="{ height: 200 }"
            option-size="small"
            class="test-plan-panel w-[240px]"
            label-path-mode
            path-mode
            popup-container=".cascader-wrapper-self"
            :load-more="loadMore"
            @change="changeHandler"
          >
            <template #label>
              <a-tooltip :content="labelPath" :mouse-enter-delay="300">
                <div class="one-line-text max-w-[200px] items-center justify-between pr-[8px]">
                  {{ labelPath }}
                </div>
              </a-tooltip>
            </template>
            <template #option="{ data }">
              <div :class="`flex ${data.isLeaf ? ' w-[130px]' : 'w-[120px]'} items-center`" title="">
                <a-tooltip :mouse-enter-delay="300" :content="t(data.label)">
                  <div class="one-line-text" title="">
                    {{ t(data.label) }}
                  </div>
                </a-tooltip>
              </div>
            </template>
          </MsCascader>
        </div>
      </div>
      <div class="my-[16px] flex items-center">
        <div class="threshold-card-item">
          <div class="threshold-card-item-name">
            <div class="flex items-center gap-[4px]">
              <a-tooltip
                :disabled="!detail?.caseCountMap?.testPlanName"
                :mouse-enter-delay="300"
                :content="detail?.caseCountMap?.testPlanName"
              >
                <div class="one-line-text max-w-[calc(100%-64px)]">{{ detail?.caseCountMap?.testPlanName ?? '-' }}</div>
              </a-tooltip>
              <MsStatusTag v-if="detail?.caseCountMap?.status" :status="detail?.caseCountMap?.status" />
            </div>
            <div class="flex w-full items-center gap-[4px]">
              <div class="w-[calc(100%-80px)]">
                <ThresholdProgress :threshold="passThreshold" :progress="passRate" :progress-color="progressColor" />
              </div>
              <div class="flex-1 text-[10px] text-[rgb(var(--success-6))]">
                {{ t('testPlan.testPlanIndex.threshold') }} {{ `${passThreshold}%` }}
              </div>
            </div>
          </div>
          <div class="char-content flex-1">
            <MsChart height="76px" width="76px" :options="execOptions" />
          </div>
        </div>
        <div class="flex-1">
          <HeaderCard :content-tab-list="cardModuleList" />
        </div>
      </div>
      <div>
        <MsChart ref="chartRef" height="280px" :options="options" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 测试计划概览  TODO 待联调
   */
  import { ref } from 'vue';
  import { CascaderOption } from '@arco-design/web-vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import bindDataZoomEvent from '@/components/pure/chart/utils';
  import MsCascader from '@/components/business/ms-cascader/index.vue';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import CardSkeleton from './cardSkeleton.vue';
  import HeaderCard from './headerCard.vue';
  import ThresholdProgress from './thresholdProgress.vue';

  import { getWorkTestPlanListUrl, workTestPlanOverviewDetail } from '@/api/modules/workbench';
  import getVisualThemeColor from '@/config/chartTheme';
  import { commonRatePieOptions } from '@/config/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { findNodePathByKey, mapTree } from '@/utils';

  import type { ModuleCardItem, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';
  import { TestPlanStatusEnum } from '@/enums/testPlanEnum';
  import { WorkOverviewEnum, WorkOverviewIconEnum } from '@/enums/workbenchEnum';

  import { createCustomTooltip, getSeriesData } from '../utils';

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
    refreshKey: number;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const appStore = useAppStore();

  const innerPlanId = defineModel<string>('planId', {
    required: true,
  });

  const innerGroupPlanId = defineModel<string>('groupId', {
    required: true,
  });

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  watch(
    () => props.item.projectIds,
    (val) => {
      innerProjectIds.value = val;
    }
  );

  const options = ref<Record<string, any>>({});

  const hasPermission = ref<boolean>(false);
  const chartRef = ref<InstanceType<typeof MsChart>>();

  const contentTabList: ModuleCardItem[] = [
    {
      label: t('workbench.homePage.functionalUseCase'),
      value: WorkOverviewEnum.FUNCTIONAL,
      icon: WorkOverviewIconEnum.FUNCTIONAL,
      color: 'rgb(var(--primary-5))',
      count: 0,
    },
    {
      label: t('workbench.homePage.interfaceCASE'),
      value: WorkOverviewEnum.API_CASE,
      icon: WorkOverviewIconEnum.API_CASE,
      color: 'rgb(var(--link-6))',
      count: 0,
    },
    {
      label: t('workbench.homePage.interfaceScenario'),
      value: WorkOverviewEnum.API_SCENARIO,
      icon: WorkOverviewIconEnum.API_SCENARIO,
      color: 'rgb(var(--link-6))',
      count: 0,
    },
    {
      label: t('workbench.homePage.bugCount'),
      value: WorkOverviewEnum.BUG_COUNT,
      icon: WorkOverviewIconEnum.BUG_COUNT,
      color: 'rgb(var(--danger-6))',
      count: 0,
    },
  ];

  const cardModuleList = ref<ModuleCardItem[]>([...contentTabList]);

  const legendContentList = [
    {
      label: 'workbench.homePage.assigningUseCases',
      value: '',
    },
    {
      label: 'workbench.homePage.completeUseCases',
      value: '',
    },
    {
      label: 'workbench.homePage.commitDefects',
      value: '',
    },
    {
      label: 'workbench.homePage.closedDefect',
      value: '',
    },
  ];

  const selectValue = ref<string[]>([]);

  const detail = ref();

  const execOptions = ref<Record<string, any>>({
    ...commonRatePieOptions,
    title: {
      ...commonRatePieOptions.title,
      left: 35,
    },
    series: {
      ...commonRatePieOptions.series,
      center: [40, '50%'],
    },
  });

  const passRate = computed(() => detail.value?.caseCountMap?.passRate ?? 0);
  const passThreshold = computed(() => detail.value?.caseCountMap?.passThreshold ?? 0);

  function handleData() {
    cardModuleList.value = contentTabList
      .map((item) => {
        return {
          ...item,
          label: t(item.label),
          count: detail.value.caseCountMap[item.value],
        };
      })
      .filter((e) => Object.keys(detail.value.caseCountMap).includes(e.value as string));

    const testPlanColor = ['#3370FF', '#00C261', '#FFA200', '#811FA3'];

    options.value = getSeriesData(legendContentList, detail.value, testPlanColor, true);

    execOptions.value.title.text = t('workbench.homePage.executeRate');
    execOptions.value.title.subtext = hasPermission.value ? `${detail.value?.caseCountMap?.executeRate ?? 0}%` : '-';

    const totalCount = detail.value?.caseCountMap?.totalCount ?? 0;
    const executeCount = detail.value?.caseCountMap?.executeCount ?? 0;
    const executeData = [
      {
        name: t('common.executed'),
        value: executeCount,
        color: '#00C261',
      },
      {
        name: t('common.unExecute'),
        value: totalCount - executeCount,
        color: getVisualThemeColor('initItemStyleColor'),
      },
    ];

    const pieBorderWidth = executeData.filter((e) => Number(e.value) > 0).length === 1 ? 0 : 1;

    const lastExecuteData = executeData
      .map((e) => {
        return {
          ...e,
          itemStyle: {
            borderWidth: pieBorderWidth,
            borderColor: getVisualThemeColor('itemStyleBorderColor'),
            color: e.color,
          },
        };
      })
      .filter((e) => e.value !== 0);

    execOptions.value.series.data =
      lastExecuteData.length > 0
        ? lastExecuteData
        : [
            {
              name: '',
              value: 1,
              tooltip: {
                show: false,
              },
              itemStyle: {
                color: getVisualThemeColor('initItemStyleColor'),
              },
            },
          ];
  }

  const showSkeleton = ref(false);
  async function initOverViewDetail() {
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
        planId: innerPlanId.value,
      };
      detail.value = await workTestPlanOverviewDetail(params);
      hasPermission.value = detail.value.errorCode !== 109001;

      handleData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }

  // 计算进度条颜色
  const progressColor = computed(() => {
    switch (detail.value?.caseCountMap?.status) {
      case TestPlanStatusEnum.PREPARED:
        return 'var(--color-text-n8)';
      case TestPlanStatusEnum.UNDERWAY:
        return 'rgb(var(--link-6))';
      case TestPlanStatusEnum.COMPLETED:
        return passRate.value < passThreshold.value ? 'rgb(var(--danger-6))' : 'rgb(var(--success-6))';
      default:
        return 'var(--color-text-n8)';
    }
  });

  const labelPath = ref<string>();
  const projectOptions = ref<{ value: string; label: string }[]>([]);
  const childrenData = ref<Record<string, CascaderOption[]>>({});

  function getLabelPath(id: string) {
    const [newProjectId] = innerProjectIds.value;
    const projectName = projectOptions.value.find((e) => e.value === newProjectId)?.label;

    const treeList = childrenData.value[newProjectId] || [];

    if (treeList.length) {
      const modules = findNodePathByKey(treeList, id, undefined, 'value');

      if (modules) {
        const moduleName = (modules || [])?.treePath.map((item: any) => item.label);
        if (moduleName.length === 1) {
          return `${projectName}/${moduleName[0]}`;
        }
        return `${projectName}/${moduleName.join(' / ')}`;
      }
    }
  }

  async function loadMore(option: CascaderOption, done: (children?: CascaderOption[]) => void) {
    try {
      let testPlanOptionsNode = await getWorkTestPlanListUrl(option.value as string);
      innerProjectIds.value = [option.value as string];
      testPlanOptionsNode = mapTree<CascaderOption>(testPlanOptionsNode, (node) => {
        return {
          ...node,
          isLeaf: true,
        };
      });

      done(testPlanOptionsNode);
      childrenData.value[option.value as string] = testPlanOptionsNode;
      labelPath.value = getLabelPath(innerPlanId.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function refreshHandler(newProjectId: string) {
    projectOptions.value = appStore.projectList.map((e) => ({ value: e.id, label: e.name }));
    const cascaderOption = projectOptions.value.find((e) => e.value === newProjectId);
    childrenData.value = {};
    if (cascaderOption) {
      loadMore(cascaderOption, (_children) => {
        childrenData.value[cascaderOption.value as string] = _children?.length ? _children : [];
        projectOptions.value = projectOptions.value.map((item) => {
          return {
            ...item,
            children: childrenData.value[item.value]?.length ? childrenData.value[item.value] : null,
            isLeaf: false,
          };
        });
      });
    }

    await initOverViewDetail();

    const chartDom = chartRef.value?.chartRef;
    if (chartDom && chartDom.chart) {
      createCustomTooltip(chartDom);
      bindDataZoomEvent(chartRef, options);
    }
  }

  async function changeHandler(value: string[]) {
    selectValue.value = value;

    innerPlanId.value = value[value.length - 1];
    const [newProjectId] = value;
    innerProjectIds.value = [newProjectId];

    const planLevel = 3;
    innerGroupPlanId.value = value.length === planLevel ? value[planLevel - 2] : '';

    await nextTick();
    refreshHandler(newProjectId);
    labelPath.value = getLabelPath(innerPlanId.value);
    emit('change');
  }

  function getSelectedParams() {
    const [newProjectId] = innerProjectIds.value;
    const selectedData = [newProjectId, innerGroupPlanId.value, innerPlanId.value];
    selectValue.value = [];
    const tempArr: string[] = [];
    selectedData.forEach((e) => {
      if (e && e !== 'NONE') {
        tempArr.push(e);
      }
    });
    return tempArr;
  }

  async function handleRefreshKeyChange() {
    await nextTick(() => {
      innerProjectIds.value = [...props.item.projectIds];
    });
    const [newProjectId] = innerProjectIds.value;
    selectValue.value = getSelectedParams();
    refreshHandler(newProjectId);
    labelPath.value = getLabelPath(innerPlanId.value);
  }

  const defaultValue = computed(() => {
    const [newProjectId] = innerProjectIds.value;
    const selectedData = [newProjectId, props.item.groupId, props.item.planId];
    const tempArr: string[] = [];
    selectedData.forEach((e) => {
      if (e && e !== 'NONE') {
        tempArr.push(e);
      }
    });
    return tempArr;
  });

  onMounted(() => {
    const [newProjectId] = props.item.projectIds;
    if (props.item.planId) {
      selectValue.value = getSelectedParams();
    }
    refreshHandler(newProjectId);
  });

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initOverViewDetail();
      }
    },
    { deep: true }
  );

  watch(() => props.refreshKey, handleRefreshKeyChange);
</script>

<style scoped lang="less">
  .card-list {
    gap: 16px;
    @apply flex  flex-1;
    .card-list-item {
      padding: 16px;
      border: 1px solid var(--color-text-n8);
      border-radius: 4px;
      @apply flex-1;
      .card-title-icon {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        @apply flex items-center justify-center;
      }
      .card-number {
        margin-left: 28px;
        font-size: 20px;
      }
    }
  }
  .threshold-card-item {
    margin-right: 16px;
    width: 34%;
    height: 78px;
    border: 1px solid var(--color-text-n8);
    border-radius: 4px;
    gap: 12px;
    @apply flex;
    .threshold-card-item-name {
      padding-left: 16px;
      width: calc(100% - 100px);
      gap: 4px;
      @apply flex flex-col justify-center;
    }
  }
  .cascader-wrapper-self {
    :deep(.arco-trigger-position-bl) {
      transform: translateX(-8%);
    }
    :deep(.arco-scrollbar) {
      transform: translateX(9%);
    }
  }
  :deep(.arco-cascader-option-label, .arco-cascader-search-option-label) {
    padding-right: 20px;
  }
</style>

<style lang="less">
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
