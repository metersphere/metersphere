<template>
  <div class="card-wrapper">
    <CardSkeleton v-if="showSkeleton" :show-skeleton="showSkeleton" />
    <div v-else>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div class="flex items-center gap-[12px]">
          <div class="text-[var(--color-text-n4)]">
            {{ t('workbench.homePage.selected') }}
            <span class="text-[rgb(var(--link-6))]">{{ innerProjectIds.length }}</span>
          </div>
          <MsSelect
            v-model:model-value="innerProjectIds"
            :options="appStore.projectList"
            allow-search
            value-key="id"
            label-key="name"
            :search-keys="['name']"
            class="!w-[230px]"
            :prefix="t('workbench.homePage.project')"
            :multiple="true"
            :has-all-select="true"
            full-tooltip-position="left"
            :default-all-select="props.item.selectAll"
            :at-least-one="true"
            @popup-visible-change="popupVisibleChange"
          >
          </MsSelect>
        </div>
      </div>
      <div class="my-[16px]">
        <HeaderCard
          :content-tab-list="cardModuleList"
          :no-permission-text="hasPermission ? '' : 'workbench.homePage.notHasResPermission'"
        />
      </div>
      <!-- 概览图 -->
      <div>
        <MsChart ref="chartRef" height="280px" :options="options" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 概览
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import bindDataZoomEvent from '@/components/pure/chart/utils';
  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import HeaderCard from './headerCard.vue';

  import { workMyCreatedDetail, workProOverviewDetail } from '@/api/modules/workbench';
  import { contentTabList } from '@/config/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type {
    ModuleCardItem,
    OverViewOfProject,
    SelectedCardItem,
    TimeFormParams,
  } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  import { createCustomTooltip, getColorScheme, getSeriesData } from '../utils';

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
    refreshKey: number;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const appStore = useAppStore();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const innerSelectAll = defineModel<boolean>('selectAll', {
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

  const cardModuleList = ref<ModuleCardItem[]>([]);

  function handleData(detail: OverViewOfProject) {
    // 处理模块顺序
    cardModuleList.value = contentTabList
      .map((item) => {
        return {
          ...item,
          label: t(item.label),
          count: detail.caseCountMap[item.value],
        };
      })
      .filter((e) => Object.keys(detail.caseCountMap).includes(e.value as string));

    options.value = getSeriesData(contentTabList, detail, getColorScheme(detail.projectCountList.length));
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
        selectAll: innerSelectAll.value,
      };
      let detail;
      if (props.item.key === WorkCardEnum.PROJECT_VIEW) {
        detail = await workProOverviewDetail(params);
      } else {
        detail = await workMyCreatedDetail(params);
      }
      hasPermission.value = detail.errorCode !== 109001;

      handleData(detail);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }
  const chartRef = ref<InstanceType<typeof MsChart>>();

  async function handleProjectChange(shouldEmit = false) {
    await nextTick();
    innerSelectAll.value = appStore.projectList.length === innerProjectIds.value.length;
    await nextTick();
    await initOverViewDetail();

    const chartDom = chartRef.value?.chartRef;

    if (chartDom && chartDom.chart) {
      createCustomTooltip(chartDom);
      bindDataZoomEvent(chartRef, options);
    }
    if (shouldEmit) emit('change');
  }

  function popupVisibleChange(val: boolean) {
    if (!val) {
      nextTick(() => {
        handleProjectChange(true);
      });
    }
  }

  async function handleRefreshKeyChange() {
    await nextTick(() => {
      innerProjectIds.value = [...props.item.projectIds];
      innerSelectAll.value = props.item.selectAll;
    });
    setTimeout(() => {
      initOverViewDetail();
    }, 0);
  }

  onMounted(async () => {
    await initOverViewDetail();
    nextTick(() => {
      const chartDom = chartRef.value?.chartRef;

      if (chartDom && chartDom.chart) {
        createCustomTooltip(chartDom);
        bindDataZoomEvent(chartRef, options);
      }
    });
  });

  onBeforeUnmount(() => {
    const unbindDataZoom = bindDataZoomEvent(chartRef, options);
    if (unbindDataZoom) {
      unbindDataZoom.clear();
    }
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
