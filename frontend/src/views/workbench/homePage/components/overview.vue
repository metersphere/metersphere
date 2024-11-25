<template>
  <div class="card-wrapper">
    <CardSkeleton v-if="showSkeleton" :show-skeleton="showSkeleton" />
    <div>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div>
          <MsSelect
            v-model:model-value="innerProjectIds"
            :options="appStore.projectList"
            allow-clear
            allow-search
            value-key="id"
            label-key="name"
            :search-keys="['name']"
            class="!w-[230px]"
            :prefix="t('workbench.homePage.project')"
            :multiple="true"
            :has-all-select="true"
            :default-all-select="innerSelectAll"
            :at-least-one="true"
            @change="changeProject"
          >
          </MsSelect>
        </div>
      </div>
      <div class="my-[16px]">
        <TabCard
          :content-tab-list="cardModuleList"
          :no-permission-text="hasPermission ? '' : 'workbench.homePage.notHasResPermission'"
        />
      </div>
      <!-- 概览图 -->
      <div>
        <MsChart height="280px" :options="options" />
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
  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import TabCard from './tabCard.vue';

  import { workMyCreatedDetail, workProOverviewDetail } from '@/api/modules/workbench';
  import { contentTabList } from '@/config/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { addCommasToNumber } from '@/utils';

  import type {
    ModuleCardItem,
    OverViewOfProject,
    SelectedCardItem,
    TimeFormParams,
  } from '@/models/workbench/homePage';
  import { WorkCardEnum, WorkOverviewEnum } from '@/enums/workbenchEnum';

  import { getColorScheme, getCommonBarOptions, handleNoDataDisplay } from '../utils';

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

  const hasRoom = computed(() => innerProjectIds.value.length >= 7 || props.item.projectIds.length === 0);

  const options = ref<Record<string, any>>({});

  const hasPermission = ref<boolean>(false);

  const cardModuleList = ref<ModuleCardItem[]>([]);

  function handleData(detail: OverViewOfProject) {
    // 处理模块顺序
    const tempAxisData = detail.xaxis.map((xAxisKey) => {
      const data = contentTabList.find((e) => e.value === xAxisKey);
      return {
        ...data,
        label: t(data?.label || ''),
        count: detail.caseCountMap[xAxisKey as WorkOverviewEnum],
      };
    });

    cardModuleList.value = tempAxisData as ModuleCardItem[];
    options.value = getCommonBarOptions(hasRoom.value, getColorScheme(detail.projectCountList.length));
    const { invisible, text } = handleNoDataDisplay(detail.xaxis, hasPermission.value);
    options.value.graphic.invisible = invisible;
    options.value.graphic.style.text = text;
    // x轴
    options.value.xAxis.data = cardModuleList.value.map((e) => e.label);

    let maxAxis = 5;

    // 处理data数据
    options.value.series = detail.projectCountList.map((item) => {
      const countData: Record<string, any>[] = item.count.map((e) => {
        return {
          name: item.name,
          value: e,
          originValue: e,
          tooltip: {
            show: true,
            trigger: 'item',
            enterable: true,
            formatter(params: any) {
              const html = `
                  <div class="w-[186px] h-[50px] p-[16px] flex items-center justify-between">
                  <div class=" flex items-center">
                  <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm bg-[${params.color}]" style="background:${
                params.color
              }"></div>
                  <div class="one-line-text max-w-[100px]"" style="color:#959598">${params.name}</div>
                  </div>
                  <div class="text-[#323233] font-medium">${addCommasToNumber(params.value)}</div>
                  </div>
                  `;
              return html;
            },
          },
        };
      });

      const itemMax = Math.max(...item.count);

      maxAxis = Math.max(itemMax, maxAxis);

      return {
        name: item.name,
        type: 'bar',
        barWidth: 12,
        legendHoverLink: true,
        large: true,
        itemStyle: {
          borderRadius: [2, 2, 0, 0], // 上边圆角
        },
        z: 10,
        data: countData,
        barMinHeight: ((optionData: Record<string, any>[]) => {
          optionData.forEach((itemValue: any, index: number) => {
            if (itemValue.value === 0) optionData[index].value = null;
          });
          let hasZero = false;
          for (let i = 0; i < optionData.length; i++) {
            if (optionData[i].value === 0) {
              hasZero = true;
              break;
            }
          }
          return hasZero ? 0 : 5;
        })(countData),
      };
    });
    options.value.yAxis[0].max = maxAxis < 100 ? 100 : maxAxis + 50;
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
  const isInit = ref(true);
  function changeProject() {
    if (isInit.value) return;
    nextTick(() => {
      emit('change');
    });
  }

  onMounted(() => {
    isInit.value = false;
    initOverViewDetail();
  });

  watch(
    () => innerProjectIds.value,
    (val) => {
      innerSelectAll.value = val.length === appStore.projectList.length;
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initOverViewDetail();
      }
    },
    {
      deep: true,
    }
  );

  watch(
    () => props.refreshKey,
    (val) => {
      if (val) {
        initOverViewDetail();
      }
    }
  );

  watch([() => props.refreshKey, () => innerProjectIds.value], async () => {
    await nextTick();
    initOverViewDetail();
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
