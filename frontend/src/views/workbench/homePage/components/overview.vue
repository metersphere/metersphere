<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t(props.item.label) }} </div>
      <div>
        <MsSelect
          v-model:model-value="innerProjectIds"
          :options="appStore.projectList"
          allow-clear
          allow-search
          value-key="id"
          label-key="name"
          :search-keys="['name']"
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
          :multiple="true"
          :has-all-select="true"
          :default-all-select="!(props.item.projectIds || []).length"
          :at-least-one="true"
        >
        </MsSelect>
      </div>
    </div>
    <div class="my-[16px]">
      <TabCard :content-tab-list="cardModuleList" />
    </div>
    <!-- 概览图 -->
    <div>
      <MsChart height="300px" :options="options" />
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
  import TabCard from './tabCard.vue';

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
  import { WorkCardEnum, WorkOverviewEnum } from '@/enums/workbenchEnum';

  import { commonColorConfig, getCommonBarOptions, handleNoDataDisplay } from '../utils';

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
  }>();

  const appStore = useAppStore();

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

  const hasRoom = computed(() => innerProjectIds.value.length >= 7);

  const options = ref<Record<string, any>>({});

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
    options.value = getCommonBarOptions(hasRoom.value, commonColorConfig);
    const { invisible, text } = handleNoDataDisplay(detail.xaxis, detail.projectCountList);
    options.value.graphic.invisible = invisible;
    options.value.graphic.style.text = text;
    // x轴
    options.value.xAxis.data = cardModuleList.value.map((e) => e.label);

    // 处理data数据
    options.value.series = detail.projectCountList.map((item) => {
      return {
        name: item.name,
        type: 'bar',
        barWidth: 12,
        itemStyle: {
          borderRadius: [2, 2, 0, 0], // 上边圆角
        },
        data: item.count,
      };
    });
  }

  async function initOverViewDetail() {
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
      let detail;
      if (props.item.key === WorkCardEnum.PROJECT_VIEW) {
        detail = await workProOverviewDetail(params);
      } else {
        detail = await workMyCreatedDetail(params);
      }

      handleData(detail);
    } catch (error) {
      console.log(error);
    }
  }

  onMounted(() => {
    initOverViewDetail();
  });

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        initOverViewDetail();
      }
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
